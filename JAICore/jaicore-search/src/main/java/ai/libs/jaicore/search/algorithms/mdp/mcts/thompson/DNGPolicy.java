package ai.libs.jaicore.search.algorithms.mdp.mcts.thompson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.common.event.IRelaxedEventEmitter;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.search.algorithms.mdp.mcts.ActionPredictionFailedException;
import ai.libs.jaicore.search.algorithms.mdp.mcts.IPathUpdatablePolicy;

/**
 * This is the implementation of the DNG-algorithm (for MDPs) presented in
 *
 * @article{bai2018posterior, title={Posterior sampling for Monte Carlo planning under uncertainty}, author={Bai, Aijun and Wu, Feng and Chen, Xiaoping}, journal={Applied Intelligence}, volume={48}, number={12}, pages={4998--5018},
 *                            year={2018}, publisher={Springer} } It does not consider rewards per step but only once for a complete roll-out
 *
 *                            Implementation details: - the time horizon H is irrelevant for the policy, because, if a horizon is used, the MCTS algorithm would not call the tree policy for cases of d >= H - we add another parameter for a
 *                            factor that will be multiplied to the sampled variance \tau
 *
 *
 * @author Felix Mohr
 *
 * @param <N>
 * @param <A>
 */
public class DNGPolicy<N, A> implements IPathUpdatablePolicy<N, A, Double>, ILoggingCustomizable, IRelaxedEventEmitter {

	private Logger logger = LoggerFactory.getLogger(DNGPolicy.class);
	private EventBus eventBus = new EventBus();

	private final boolean maximize;

	/* initialization according to section 6.2 in the paper */
	private final double initLambda;
	private static final double INIT_ALPHA = 1.0;
	private final double initBeta;
	private static final double INIT_MU = 0.5; // we set this to .5 since we already know that scores are in [0,1]

	/* DNG model parameters */
	private final Map<N, Double> alpha = new HashMap<>();
	private final Map<N, Double> beta = new HashMap<>();
	private final Map<N, Double> mu = new HashMap<>();
	private final Map<N, Double> lambda = new HashMap<>();
	private final Map<N, Map<A, Map<N, Integer>>> rho = new HashMap<>();

	/* MDP-related variables */
	private final double gammaMDP; // the discount factor of the MDP
	private final Predicate<N> terminalStatePredicate; // this policy needs to know a bit of the MDP: it needs to know whether or not a state is terminal
	private final Map<N, Map<A, Double>> rewardsMDP = new HashMap<>(); // memorizes the rewards observed for an action in the MDP

	private final double varianceFactor;
	private boolean sampling = true; // can be deactivated after using the policy to only use the final model

	public DNGPolicy(final double gammaMDP, final Predicate<N> terminalStatePredicate, final double varianceFactor, final double lambda, final boolean maximize) {
		super();
		this.gammaMDP = gammaMDP;
		this.terminalStatePredicate = terminalStatePredicate;
		this.varianceFactor = varianceFactor;
		this.initLambda = lambda;
		this.initBeta = 1 / this.initLambda;
		this.maximize = maximize;
	}

	public boolean isSampling() {
		return this.sampling;
	}

	public void setSampling(final boolean sampling) {
		this.sampling = sampling;
	}

	@Override
	/**
	 * This is the first part of the ELSE branch in the MCTS algorithm of the paper (calling Thompson sampling to choose A). The "simulation" part is taken over by the general MCTS routine
	 */
	public A getAction(final N node, final Collection<A> actionsWithSuccessors) throws ActionPredictionFailedException, InterruptedException {
		return this.sampleWithThompson(node, actionsWithSuccessors);
	}

	/**
	 * The ThompsonSampling procedure of the paper
	 *
	 * @param state
	 * @param actions
	 * @return
	 * @throws InterruptedException
	 * @throws ObjectEvaluationFailedException
	 */
	public A sampleWithThompson(final N state, final Collection<A> actions) throws InterruptedException {
		A bestAction = null;
		this.logger.info("Determining best action for state {}", state);
		double bestScore = (this.maximize ? -1 : 1) * Double.MAX_VALUE;
		for (A action : actions) {
			double score = this.getQValue(state, action);
			this.logger.debug("Score for action {} is {}", action, score);
			this.eventBus.post(new DNGQSampleEvent<N, A>(null, state, action, score));
			if (bestAction == null || (score < bestScore || (this.maximize && score > bestScore))) {
				bestAction = action;
				bestScore = score;
				this.logger.debug("Considering this as the new best action.");
			}
		}
		Objects.requireNonNull(bestAction, "Best action cannot be null if there were " + actions.size() + " options!");
		this.logger.info("Recommending action {}", bestAction);
		return bestAction;
	}

	/**
	 * In the deterministic case (and when transitions are clear), without discounts, and inner rewards = 0, the QValue function in the paper degenerates to just returning the value of the successor state of the given state.
	 *
	 * @param state
	 *            This is not needed and may be null; is rather for documentation here.
	 * @param successorState
	 * @return
	 * @throws InterruptedException
	 * @throws ObjectEvaluationFailedException
	 */
	public double getQValue(final N state, final A action) throws InterruptedException {

		/* make sure that the rho-values are available to estimate the transition probabilities */
		Map<N, Integer> rhoForThisPair = this.rho.get(state).get(action);
		if (rhoForThisPair == null) {
			throw new IllegalStateException("Have no rho vector for state/action pair " + state + "/" + action);
		}
		List<N> possibleSuccessors = new ArrayList<>(rhoForThisPair.keySet());
		int numSuccessors = possibleSuccessors.size();
		if (rhoForThisPair.size() < numSuccessors) {
			throw new IllegalStateException("The rho vector for state/action pair " + state + "/" + action + " is incomplete and only has " + rhoForThisPair.size() + " instead of " + numSuccessors + " entries.");
		}

		/* compute score */
		double r = 0;
		this.logger.debug("Now determining q-value of action {}. Sampling: {}", action, this.sampling);
		if (this.sampling) { // draw weight vector from Dirichlet (we sample using sampling from a Gamma distribution)
			double[] gammaVector = new double[numSuccessors];
			double totalGammas = 0;
			for (int i = 0; i < numSuccessors; i++)  {
				N succ = possibleSuccessors.get(i);
				double gamma = new GammaDistribution(rhoForThisPair.get(succ), 1).sample();
				gammaVector[i] = gamma;
				totalGammas += gamma;
			}
			if (totalGammas == 0) {
				throw new IllegalStateException("The gamma estimates must not sum up to 0!");
			}
			for (int i = 0; i < numSuccessors; i++)  {
				r += gammaVector[i] / totalGammas * this.getValue(possibleSuccessors.get(i)); // the first factor is the Dirichlet estimate
			}
		}
		else {
			double denominator = rhoForThisPair.values().stream().reduce((a, b) -> a + b).get();
			for (N succ : possibleSuccessors) {
				r += rhoForThisPair.get(succ) / denominator;
			}
		}
		double reward = this.rewardsMDP.get(state).get(action);
		double totalReward = reward + this.gammaMDP * r;
		this.logger.debug("Considering a reward of {} + {} * {} = {}", reward, this.gammaMDP, r, totalReward);
		return totalReward;
	}

	public Pair<Double, Double> sampleWithNormalGamma(final N state) {
		double tau = new GammaDistribution(this.alpha.get(state), this.beta.get(state)).sample();
		double std = 1 / (this.lambda.get(state) * tau);
		double muNew = std > 0 ? new NormalDistribution(this.mu.get(state), std).sample() : this.mu.get(state);
		return new Pair<>(muNew, tau);
	}

	/**
	 * The Value procedure of the paper
	 *
	 * @return
	 * @throws InterruptedException
	 * @throws ObjectEvaluationFailedException
	 */
	public double getValue(final N state) throws InterruptedException {
		boolean isTerminal = this.terminalStatePredicate.test(state);
		if (Thread.interrupted()) {
			throw new InterruptedException();
		}
		if (isTerminal) { // this assumes that terminal states are also goal states!
			this.logger.debug("Returning value of 0 for terminal state {}", state);
			return 0;
		} else if (this.sampling) {
			Pair<Double, Double> meanAndVariance = this.sampleWithNormalGamma(state);
			double val = meanAndVariance.getX() - this.varianceFactor * Math.sqrt(meanAndVariance.getY());
			this.logger.debug("Returning sampled value of {}", val);
			return val;
		}
		else {
			double val = this.mu.get(state);
			this.logger.debug("Returning fixed value of {}", val);
			return val;
		}
	}

	@Override
	/**
	 * This is the update section of the algorithm, which can be found in the ELSE-branch on the left of Fig. 1 (lines 21-25)
	 */
	public void updatePath(final ILabeledPath<N, A> path, final List<Double> scores) {
		List<N> nodes = path.getNodes();
		List<A> actions = path.getArcs();
		int l = path.getNumberOfNodes();
		this.logger.info("Updating path with scores {}", scores);

		double accumulatedScores = 0;
		for (int i = l - 2; i >= 0; i --) {
			N node = nodes.get(i);
			A action = actions.get(i);
			double rewardOfThisAction = scores.get(i) != null ? scores.get(i) : Double.NaN;
			this.rewardsMDP.computeIfAbsent(node, n -> new HashMap<>()).putIfAbsent(action, rewardOfThisAction);
			accumulatedScores = rewardOfThisAction + this.gammaMDP * accumulatedScores;
			this.logger.debug("Updating statistics for {}-th node with accumulated score {}. State here is: {}", i, accumulatedScores, node);

			/* if we had no model for this node yet, create an empty one */
			if (!this.lambda.containsKey(node)) {

				/* NormalGamma parameters */
				this.lambda.put(node, this.initLambda);
				this.mu.put(node, INIT_MU);
				this.alpha.put(node, INIT_ALPHA);
				this.beta.put(node, this.initBeta);

				/* rho-parameter */
				N succNode = nodes.get(i + 1);
				Map<N, Integer> rhoForNodeActionPair = new HashMap<>();
				rhoForNodeActionPair.put(succNode, 1);
				Map<A, Map<N, Integer>> mapForAction = new HashMap<>();
				mapForAction.put(action, rhoForNodeActionPair);
				this.rho.put(node, mapForAction);
			}
			else {

				/* update model parameters */
				double lambdaOfN = this.lambda.get(node);
				double muOfN = this.mu.get(node);
				this.alpha.put(node, this.alpha.get(node) + 0.5);
				this.beta.put(node, this.beta.get(node) + (lambdaOfN * Math.pow(accumulatedScores - muOfN, 2) / (lambdaOfN + 1)) / 2);
				this.mu.put(node, (muOfN * lambdaOfN + accumulatedScores) / (lambdaOfN + 1));
				this.lambda.put(node, lambdaOfN + 1);
				N succNode = nodes.get(i + 1);
				this.rho.get(node).computeIfAbsent(action, a -> new HashMap<>()).put(succNode, this.rho.get(node).get(action).computeIfAbsent(succNode, n -> 0) + 1);

				this.eventBus.post(new DNGBeliefUpdateEvent<N>(null, node, this.mu.get(node), this.alpha.get(node), this.beta.get(node), this.lambda.get(node)));
			}
		}
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Logger is now {}", name);
	}

	@Override
	public void registerListener(final Object listener) {
		this.eventBus.register(listener);
	}
}

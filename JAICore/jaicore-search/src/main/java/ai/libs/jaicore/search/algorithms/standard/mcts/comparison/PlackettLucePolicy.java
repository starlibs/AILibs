package ai.libs.jaicore.search.algorithms.standard.mcts.comparison;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.common.event.IRelaxedEventEmitter;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;
import ai.libs.jaicore.graph.LabeledGraph;
import ai.libs.jaicore.math.probability.pl.PLInferenceProblem;
import ai.libs.jaicore.math.probability.pl.PLInferenceProblemEncoder;
import ai.libs.jaicore.math.probability.pl.PLMMAlgorithm;
import ai.libs.jaicore.search.algorithms.standard.mcts.ActionPredictionFailedException;
import ai.libs.jaicore.search.algorithms.standard.mcts.IGraphDependentPolicy;
import ai.libs.jaicore.search.algorithms.standard.mcts.IPathUpdatablePolicy;
import ai.libs.jaicore.search.algorithms.standard.mcts.UniformRandomPolicy;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class PlackettLucePolicy<N, A> implements IPathUpdatablePolicy<N, A, Double>, ILoggingCustomizable, IGraphDependentPolicy<N, A>, IRelaxedEventEmitter {

	private final EventBus eventBus = new EventBus();
	private Logger logger = LoggerFactory.getLogger(PlackettLucePolicy.class);
	private final IPreferenceKernel<N, A> preferenceKernel;
	private final Map<N, DoubleList> skillVectorsForNodes = new HashMap<>();
	private final Map<N, Integer> numVisits = new HashMap<>();
	private final Map<N, Double> deepestRelativeNodeDepthsOfNodes = new HashMap<>();
	private final Map<N, Double> lastLocalProbabilityOfNode = new HashMap<>();
	private final Random random;
	private final UniformRandomPolicy<N, A, Double> randomPolicy;
	private LabeledGraph<N, A> graph;
	private final IGammaFunction gammaShort = new CosLinGammaFunction(3, 4, 2, 2);
	private static final double EPSILON = 0.0;
	private IOwnerBasedAlgorithmConfig config = ConfigFactory.create(IOwnerBasedAlgorithmConfig.class);

	/* configuration of gamma-shape. this depends on the branching factor.
	 * Note that "per child" does not mean that each child needs so many visits but for k children, the parent needs k * p observations. */
	private static final int GAMMA_LONG_MAX = 5;
	private static final int GAMMA_LONG_MIN_OBSERVATIONS_PER_CHILD_FOR_SUPPORT_INIT = 5;
	private static final int GAMMA_LONG_MIN_OBSERVATIONS_PER_CHILD_FOR_SUPPORT_ABS = 2;
	private static final int GAMMA_LONG_OBSERVATIONS_PER_CHILD_FOR_ONE = 10;
	private static final int GAMMA_LONG_OBSERVATIONS_PER_CHILD_FOR_MAX = 20;
	public PlackettLucePolicy(final IPreferenceKernel<N, A> preferenceKernel, final Random random) {
		super();
		this.preferenceKernel = preferenceKernel;
		this.random = random;
		this.randomPolicy = new UniformRandomPolicy<>(new Random(random.nextLong()));
	}

	@Override
	public A getAction(final N node, final Map<A, N> actionsWithSuccessors) throws ActionPredictionFailedException {

		if (!this.preferenceKernel.canProduceReliableRankings(node)) {
			this.logger.info("Preference kernel tells us that it cannot produce reliable information yet. Choosing one action at random.");
			return this.randomPolicy.getAction(node, actionsWithSuccessors);
		}

		/* get likelihood for children */
		try {

			/* determine gamma function for this node, and then evaluate it to get the correct gamma value */
			int visits = this.numVisits.get(node);
			int numChildren = this.graph.getSuccessors(node).size();
			this.logger.info("Computing action for node {} with {} successors.", node, numChildren);
			IGammaFunction gammaFunction = new CombinedGammaFunction(this.gammaShort, new CosLinGammaFunction(GAMMA_LONG_MAX, numChildren * GAMMA_LONG_OBSERVATIONS_PER_CHILD_FOR_ONE,  numChildren * GAMMA_LONG_MIN_OBSERVATIONS_PER_CHILD_FOR_SUPPORT_INIT, numChildren * GAMMA_LONG_MIN_OBSERVATIONS_PER_CHILD_FOR_SUPPORT_ABS));
			double relativeDepth = this.deepestRelativeNodeDepthsOfNodes.get(node);
			double nodeProbability = this.getProbabilityOfNode(node);
			double gammaValue = gammaFunction.getNodeGamma(visits, nodeProbability, relativeDepth);

			/* check whether there is only one child */
			if (numChildren <= 1) {
				if (numChildren < 1) {
					throw new UnsupportedOperationException("Cannot compute action for nodes without successors.");
				}
				if (actionsWithSuccessors.size() != 1) {
					throw new IllegalStateException();
				}
				return actionsWithSuccessors.keySet().iterator().next();
			}

			/* check whether epsilon forces us to explore */
			if (this.random.nextDouble() < EPSILON * (1 - relativeDepth)) {
				return this.randomPolicy.getAction(node, actionsWithSuccessors);
			}

			/* estimate PL-parameters */
			this.logger.debug("Computing PL-Problem instance");
			PLInferenceProblemEncoder encoder = new PLInferenceProblemEncoder();
			PLInferenceProblem problem = encoder.encode(this.preferenceKernel.getRankingsForChildrenOfNode(node));
			DoubleList skills;
			if (gammaValue != 0) {
				this.logger.debug("Start computation of skills for {}. Using {} rankings based on {} visits. Gamma value is {} based on node probability {} and depth {}", node, problem.getRankings().size(), visits, gammaValue, nodeProbability, relativeDepth);
				skills = new PLMMAlgorithm(problem, this.skillVectorsForNodes.get(node), this.config).call();
				this.skillVectorsForNodes.put(node, skills);
			}
			else {
				skills = PLMMAlgorithm.getDefaultSkillVector(problem.getNumObjects());
			}
			if (skills.size() != problem.getNumObjects()) {
				throw new IllegalStateException("Have " + skills.size() + " skills (" + skills + ") for " + problem.getNumObjects() + " objects.");
			}

			/* adjust PL-parameters according to gamma */
			int n = skills.size();
			double sum = 0;
			for (int i = 0; i < n; i++) {
				double newVal = Math.pow(skills.getDouble(i), gammaValue);
				skills.set(i, newVal);
				sum += newVal;
			}
			if (sum == 0) {
				throw new IllegalStateException();
			}
			for (int i = 0; i < n; i++) {
				if (this.logger.isDebugEnabled()) {
					this.logger.debug("Estimating skill of successor {} with action {} and {} visits by {} -> {}", i, encoder.getObjectAtIndex(i), this.numVisits.get(encoder.getObjectAtIndex(i)), skills.getDouble(i), skills.getDouble(i) / sum);
				}
				skills.set(i, skills.getDouble(i) / sum);
			}

			/* compute mass of actually available options */
			double massOfRemainingOptions = 1;
			if (actionsWithSuccessors.size() != skills.size()) {
				massOfRemainingOptions = 0;
				for (Entry<A, N> entry : actionsWithSuccessors.entrySet()) {
					massOfRemainingOptions += skills.getDouble(encoder.getIndexOfObject(entry.getValue()));
				}
				if (massOfRemainingOptions == 0) {
					this.logger.info("Choosing option with prob 0");
					return actionsWithSuccessors.keySet().iterator().next();
				}
			}

			/* draw random action */
			double randomNumber = this.random.nextDouble() * massOfRemainingOptions;
			sum = 0;
			A succ = null;
			for (Entry<A, N> entry : actionsWithSuccessors.entrySet()) {
				double newProbOfNode = skills.getDouble(encoder.getIndexOfObject(entry.getValue()));
				if (Double.isNaN(newProbOfNode)) {
					this.logger.error("Probability of successor is NaN! Skill vector: {}", skills);
				}
				this.lastLocalProbabilityOfNode.put(entry.getValue(), newProbOfNode);
				sum += newProbOfNode;
				if (succ == null && sum >= randomNumber) {
					succ = entry.getKey();
					this.logger.debug("Chose successor {} with skill {}", succ, newProbOfNode);
				}
			}
			if (succ == null) {
				throw new IllegalStateException("Could not find child among " + actionsWithSuccessors.size() + " successors. Mass of remaining options is " + massOfRemainingOptions + ". Drawn random number is " + randomNumber + ". Sum of skimmed probs is " + sum);
			}
			return succ;
		} catch (AlgorithmTimeoutedException | AlgorithmExecutionCanceledException | AlgorithmException e) {
			throw new ActionPredictionFailedException(e);
		} catch (InterruptedException e) {
			this.logger.info("Policy thread has been interrupted. Re-interrupting thread, because no InterruptedException can be thrown here.");
			Thread.currentThread().interrupt();
			return null;
		}
	}

	private double getProbabilityOfNode(final N node) {
		N curNode = node;
		double prob = 1;
		while (!this.graph.getPredecessors(curNode).isEmpty()) {
			if (this.lastLocalProbabilityOfNode.containsKey(curNode)) {
				prob *= this.lastLocalProbabilityOfNode.get(curNode);
			}
			else {
				this.logger.warn("No probability known for node {}", curNode);
			}
			curNode = this.graph.getPredecessors(curNode).iterator().next();
		}
		return prob;
	}

	@Override
	public void registerListener(final Object listener) {
		this.eventBus.register(listener);
	}

	@Override
	public void setGraph(final LabeledGraph<N, A> graph) {
		this.graph = graph;
		this.preferenceKernel.setExplorationGraph(graph);
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		if (this.preferenceKernel instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.preferenceKernel).setLoggerName(name + ".kernel");
		}
	}

	@Override
	public void updatePath(final ILabeledPath<N, A> path, final Double playoutScore, final int lengthOfPlayout) {
		this.preferenceKernel.signalNewScore(path, playoutScore);
		int depth = 0;
		for (N node : path.getNodes()) {
			double relativeDepth = depth * 1.0 / lengthOfPlayout;
			this.numVisits.put(node, this.numVisits.computeIfAbsent(node, n -> 0) + 1);
			if (!this.deepestRelativeNodeDepthsOfNodes.containsKey(node) || this.deepestRelativeNodeDepthsOfNodes.get(node) < relativeDepth) {
				this.deepestRelativeNodeDepthsOfNodes.put(node, relativeDepth);
			}
			depth ++;
		}
	}

}

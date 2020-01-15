package ai.libs.jaicore.search.algorithms.standard.mcts.thompson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.INodeGoalTester;
import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.common.event.IRelaxedEventEmitter;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.search.algorithms.standard.mcts.ActionPredictionFailedException;
import ai.libs.jaicore.search.algorithms.standard.mcts.IPathUpdatablePolicy;

/**
 * This is a slightly simplified implementation of the algorithm presented in
 *
 * @article{bai2018posterior, title={Posterior sampling for Monte Carlo planning under uncertainty}, author={Bai, Aijun and Wu, Feng and Chen, Xiaoping}, journal={Applied Intelligence}, volume={48}, number={12}, pages={4998--5018},
 *                            year={2018}, publisher={Springer} } It does not consider rewards per step but only once for a complete roll-out
 *
 *
 * @author felix
 *
 * @param <N>
 * @param <A>
 */
public class DNGPolicy<N, A> implements IPathUpdatablePolicy<N, A, Double>, ILoggingCustomizable, IRelaxedEventEmitter {

	private Logger logger = LoggerFactory.getLogger(DNGPolicy.class);
	private EventBus eventBus = new EventBus();

	/* initialization according to section 6.2 in the paper */
	private final double initLambda;
	private final double initAlpha = 1.0;
	private final double initBeta;
	private final double initMu = 1.0;

	private final Map<N, Double> alpha = new HashMap<>();
	private final Map<N, Double> beta = new HashMap<>();
	private final Map<N, Double> mu = new HashMap<>();
	private final Map<N, Double> lambda = new HashMap<>();
	private final boolean metricTracking = false;
	private final Map<N, DescriptiveStatistics> statsPerNode = new HashMap<>();
	private final Map<String, Integer> winsPerMetric = new HashMap<>();

	private final INodeGoalTester<N, A> goalTester;
	private final IObjectEvaluator<N, Double> leafNodeEvaluator;
	private final double varianceFactor;

	public DNGPolicy(final INodeGoalTester<N, A> goalTester, final IObjectEvaluator<N, Double> leafNodeEvaluator, final double varianceFactor, final double lambda) {
		super();
		this.goalTester = goalTester;
		this.leafNodeEvaluator = leafNodeEvaluator;
		this.varianceFactor = varianceFactor;
		this.initLambda = lambda;
		this.initBeta = 1 / this.initLambda;
	}

	@Override
	/**
	 * This is the first part of the ELSE branch in the MCTS algorithm of the paper (calling Thompson sampling to choose A).
	 * The "simulation" part is taken over by the general MCTS routine
	 */
	public A getAction(final N node, final Map<A, N> actionsWithSuccessors) throws ActionPredictionFailedException {
		try {
			return this.sampleWithThompson(node, actionsWithSuccessors);
		} catch (ObjectEvaluationFailedException | InterruptedException e) {
			throw new ActionPredictionFailedException(e);
		}
	}

	@Override
	/**
	 * This is the part of the ELSE branch of the MCTS algorithm of the paper that FOLLOWS the simulation.
	 * In fact, without inner rewards and without discounting, this just amounts to use the playout score
	 * for all nodes on the path, which can be done in a simple loop.
	 */
	public void updatePath(final ILabeledPath<N, A> path, final Double playoutScore, final int lengthOfPlayoutPath) {
		for (N node : path.getNodes()) {
			double lambdaOfN = this.lambda.computeIfAbsent(node, n -> this.initLambda);
			double muOfN = this.mu.computeIfAbsent(node, n -> this.initMu);
			this.alpha.put(node, this.alpha.computeIfAbsent(node, n -> this.initAlpha) + 0.5);
			this.beta.put(node, this.beta.computeIfAbsent(node, n -> this.initBeta) + (lambdaOfN * Math.pow(playoutScore - muOfN, 2) / (lambdaOfN + 1)) / 2);
			this.mu.put(node, (muOfN * lambdaOfN + playoutScore) / (lambdaOfN + 1));
			this.lambda.put(node, lambdaOfN + 1);
			this.eventBus.post(new DNGBeliefUpdateEvent<N>(null, node, this.mu.get(node), this.alpha.get(node), this.beta.get(node), this.lambda.get(node)));
			if (this.metricTracking) {
				this.statsPerNode.computeIfAbsent(node, n -> new DescriptiveStatistics()).addValue(playoutScore);
			}
		}
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
	public A sampleWithThompson(final N state, final Map<A, N> actions) throws ObjectEvaluationFailedException, InterruptedException {
		A bestAction = null;
		double bestScore = Double.MAX_VALUE;

		/* prepare metrics for analysis */
		if (this.metricTracking) {
			Map<String, Function<DescriptiveStatistics, Double>> metrics = new HashMap<>();
			metrics.put("avg", d -> d.getMean());;
			metrics.put("avg-std", d -> d.getMean() - d.getStandardDeviation());
			metrics.put("geomean", d -> d.getGeometricMean());
			metrics.put("n", d -> (double)d.getN());
			metrics.put("min", d -> d.getMin());
			metrics.put(".25p", d -> d.getPercentile(25));
			metrics.put(".5p", d -> d.getPercentile(50));
			metrics.put(".75p", d -> d.getPercentile(75));
			metrics.put("max", d -> d.getMax());
			Map<String, List<A>> successorOrderingByMetrics = new HashMap<>();
			for (Entry<String, Function<DescriptiveStatistics, Double>> metricEntry : metrics.entrySet()) {
				successorOrderingByMetrics.put(metricEntry.getKey(), actions.keySet().stream().sorted((v1, v2) -> {
					return Double.compare(metricEntry.getValue().apply(this.statsPerNode.get(actions.get(v1))), metricEntry.getValue().apply(this.statsPerNode.get(actions.get(v2))));
				}).collect(Collectors.toList()));
			}
		}

		for (Entry<A, N> actionStatePair : actions.entrySet()) {
			double score = this.getQValue(state, actionStatePair.getValue());
			if (this.metricTracking) {
				DescriptiveStatistics stats = this.statsPerNode.get(actionStatePair.getValue());
				if (this.logger.isDebugEnabled()) {
					this.logger.debug("Sampled score {} for action {} with successor {}. Stats for this node is min/mean (believed)/max {}/{} ({})/{} with {} visits. Deviation in estimation: {}", score, actionStatePair.getKey(), actionStatePair.getValue(), stats.getMin(), stats.getMean(), this.mu.get(actionStatePair.getValue()), stats.getMax(), stats.getN(), this.mu.get(actionStatePair.getValue()) - stats.getMean());
				}
			}
			this.eventBus.post(new DNGQSampleEvent<N, A>(null, state, actionStatePair.getValue(), actionStatePair.getKey(), score));
			if (score < bestScore) {
				bestAction = actionStatePair.getKey();
				bestScore = score;
			}
		}

		/* log metrics about this choice */
		//		if (this.metricTracking) {
		//			String bestMetric = null;
		//			int bestRank = 10000;
		//			for (Entry<String, List<A>> metricOrderingPair : successorOrderingByMetrics.entrySet()) {
		//				A bestActionForThisMetric = metricOrderingPair.getValue().get(0);
		//				int rankOfChoiceForThisMetric = metricOrderingPair.getValue().indexOf(bestAction);
		//				if (rankOfChoiceForThisMetric < bestRank) {
		//					bestRank = rankOfChoiceForThisMetric;
		//					bestMetric = metricOrderingPair.getKey();
		//				}
		//				double scoreOfBest = metrics.get(metricOrderingPair.getKey()).apply(this.statsPerNode.get(actions.get(bestActionForThisMetric)));
		//				double scoreOfChosen = metrics.get(metricOrderingPair.getKey()).apply(this.statsPerNode.get(actions.get(bestAction)));
		//				this.logger.debug("Choice for metric {}: {}/{}. Epsilon to optimum: {}", metricOrderingPair.getKey(), rankOfChoiceForThisMetric, actions.size(), Math.abs(scoreOfChosen - scoreOfBest));
		//			}
		//			this.winsPerMetric.put(bestMetric, this.winsPerMetric.computeIfAbsent(bestMetric, m -> 0) + 1);
		//			this.logger.debug("Wins per metric: {}", this.winsPerMetric);
		//		}
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
	public double getQValue(final N state, final N successorState) throws ObjectEvaluationFailedException, InterruptedException {
		return this.getValue(successorState);
	}

	public Pair<Double, Double> sampleWithNormalGamma(final N state) {
		double tau = new GammaDistribution(this.alpha.get(state), this.beta.get(state)).sample();
		double muNew = new NormalDistribution(this.mu.get(state), 1 / (this.lambda.get(state) * tau)).sample();
		return new Pair<>(muNew, tau);
	}

	/**
	 * The Value procedure of the paper
	 *
	 * @return
	 * @throws InterruptedException
	 * @throws ObjectEvaluationFailedException
	 */
	public double getValue(final N state) throws ObjectEvaluationFailedException, InterruptedException {
		if (this.goalTester.isGoal(state)) {
			return this.leafNodeEvaluator.evaluate(state);
		}
		else {
			Pair<Double, Double> meanAndVariance = this.sampleWithNormalGamma(state);
			return meanAndVariance.getX() - this.varianceFactor * Math.sqrt(meanAndVariance.getY());
		}
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}

	@Override
	public void registerListener(final Object listener) {
		this.eventBus.register(listener);
	}
}

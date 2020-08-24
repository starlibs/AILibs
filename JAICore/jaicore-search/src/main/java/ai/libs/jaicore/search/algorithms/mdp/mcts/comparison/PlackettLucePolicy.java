package ai.libs.jaicore.search.algorithms.mdp.mcts.comparison;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.common.event.IRelaxedEventEmitter;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;
import ai.libs.jaicore.basic.MathExt;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.graph.LabeledGraph;
import ai.libs.jaicore.graphvisualizer.events.graph.GraphEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodePropertyChangedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeRemovedEvent;
import ai.libs.jaicore.math.probability.pl.PLInferenceProblem;
import ai.libs.jaicore.math.probability.pl.PLInferenceProblemEncoder;
import ai.libs.jaicore.math.probability.pl.PLMMAlgorithm;
import ai.libs.jaicore.search.algorithms.mdp.mcts.ActionPredictionFailedException;
import ai.libs.jaicore.search.algorithms.mdp.mcts.IPathUpdatablePolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.IRolloutLimitDependentPolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.UniformRandomPolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.comparison.preferencekernel.bootstrapping.BootstrappingPreferenceKernel;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class PlackettLucePolicy<N, A> implements IPathUpdatablePolicy<N, A, Double>, ILoggingCustomizable, IRelaxedEventEmitter, IRolloutLimitDependentPolicy {

	private boolean hasListeners = false;
	private final EventBus eventBus = new EventBus();

	private Logger logger = LoggerFactory.getLogger(PlackettLucePolicy.class);
	private final IPreferenceKernel<N, A> preferenceKernel;
	private final Set<N> nodesForWhichAnActionHasBeenRequested = new HashSet<>();
	private final LabeledGraph<N, A> activeTree = new LabeledGraph<>(); // maintain back pointers and actions for nodes relevant to the policy
	private final Map<N, Map<A, Double>> skillsForActions = new HashMap<>();
	private final Map<N, Map<A, Integer>> numPulls = new HashMap<>();
	private final Map<N, A> fixedDecisions = new HashMap<>();
	private final Map<N, Pair<A, Integer>> sequentialCertaintyCounts = new HashMap<>();
	private final Map<N, Double> deepestRelativeNodeDepthsOfNodes = new HashMap<>();
	private final Map<N, Map<A, Double>> lastLocalProbabilityOfNode = new HashMap<>();
	private final Map<N, Integer> depths = new HashMap<>();
	private final Random random;
	private final UniformRandomPolicy<N, A, Double> randomPolicy;
	private final IGammaFunction gammaShort = new CosLinGammaFunction(3, 4, 2, 2);
	private static final double EPSILON = 0.0;
	private IOwnerBasedAlgorithmConfig config = ConfigFactory.create(IOwnerBasedAlgorithmConfig.class);

	private final Map<N, Integer> maxChildren = new HashMap<>();
	private double avgDepth;
	private double avgTargetDepth;
	private double avgBranchingFactor = -1;
	private int numUpdates;
	private int expectedNumberOfTotalRollouts;
	private double estimateForSlopeForTargetValueOfGammaEqualsOne;

	/* configuration of gamma-shape. this depends on the branching factor.
	 * Note that "per child" does not mean that each child needs so many visits but for k children, the parent needs k * p observations. */
	private static final int GAMMA_LONG_MAX = 1;
	private static final int GAMMA_LONG_MIN_OBSERVATIONS_PER_CHILD_FOR_SUPPORT_INIT = 10;
	private static final int GAMMA_LONG_MIN_OBSERVATIONS_PER_CHILD_FOR_SUPPORT_ABS = 10;
	private static final int GAMMA_LONG_OBSERVATIONS_PER_CHILD_TO_REACH_ONE = 10;
	private static final double GAMMA_LONG_DERIVATIVE_EXP = 2;
	private static final double GOAL_COMMIT_DEPTH = .9;

	private static final int MINIMUMNUMBERTOCOMMIT = 20;

	public PlackettLucePolicy(final IPreferenceKernel<N, A> preferenceKernel, final Random random) {
		super();
		this.preferenceKernel = preferenceKernel;
		this.random = random;
		this.randomPolicy = new UniformRandomPolicy<>(new Random(random.nextLong()));
		if (preferenceKernel instanceof IRelaxedEventEmitter) {
			((IRelaxedEventEmitter) preferenceKernel).registerListener(new Object() {

				@Subscribe
				public void receiveEvent(final GraphEvent event) {
					PlackettLucePolicy.this.eventBus.post(event);
				}
			});
		}
	}

	public CombinedGammaFunction getGammaFunction(final N node, final Collection<A> actions) {
		int k = actions.size();
		int depth = this.depths.get(node);
		int obsRequiredForOne = depth - this.fixedDecisions.size() < this.avgTargetDepth ? (int) Math.ceil(this.getNumberOfSamplesPerChildForGamma(depth - this.fixedDecisions.size())) : 1;
		double blowup = 1.0;// 2.0 * (1 - this.numUpdates * 1.0 / this.expectedNumberOfTotalRollouts);
		obsRequiredForOne *= k;
		obsRequiredForOne *= blowup;
		IGammaFunction definiteDecisionGammaFunction = new CosLinGammaFunction(GAMMA_LONG_MAX, Math.max(GAMMA_LONG_OBSERVATIONS_PER_CHILD_TO_REACH_ONE * k, (int) Math.ceil(obsRequiredForOne)),
				Math.max(GAMMA_LONG_MIN_OBSERVATIONS_PER_CHILD_FOR_SUPPORT_INIT * k, (int) Math.ceil(0.5 * obsRequiredForOne)), k * GAMMA_LONG_MIN_OBSERVATIONS_PER_CHILD_FOR_SUPPORT_ABS);// new
		return new CombinedGammaFunction(this.gammaShort, definiteDecisionGammaFunction);
	}

	public double getGammaValue(final N node, final Collection<A> actions) {
		Map<A, Integer> numPullsPerAction = this.numPulls.get(node);
		int visits = numPullsPerAction.values().stream().reduce((a, b) -> a + b).get();
		IGammaFunction gammaFunction = this.getGammaFunction(node, actions);
		double relativeDepth = this.deepestRelativeNodeDepthsOfNodes.get(node);
		double nodeProbability = this.getProbabilityOfNode(node);
		return gammaFunction.getNodeGamma(visits, nodeProbability, relativeDepth);
	}

	@Override
	public A getAction(final N node, final Collection<A> actions) throws ActionPredictionFailedException {
		long start = System.currentTimeMillis();
		Map<String, Object> nodeInfoMap = new HashMap<>();
		int depth = this.depths.get(node);
		this.logger.info("Determining action for node in depth {}. {} actions available. Node info: {}", depth, actions.size(), node);
		if (!this.nodesForWhichAnActionHasBeenRequested.contains(node)) {

			/* memorize that this node has been relevant for decision */
			this.nodesForWhichAnActionHasBeenRequested.add(node);

			/* inform kernel about this new node  */
			this.preferenceKernel.signalNodeActiveness(node);

			if (!this.maxChildren.containsKey(node)) {
				this.maxChildren.put(node, actions.size());
			}
		}

		/* disable PL policy for nodes in which x times in a row the same action was recommended with a high probability (then always return that action) */
		if (this.fixedDecisions.containsKey(node)) {
			this.logger.info("Choosing fixed action {}", this.fixedDecisions.get(node));
			return this.fixedDecisions.get(node);
		}

		/* check whether we can produce a useful information */
		if (!this.preferenceKernel.canProduceReliableRankings(node, actions)) {
			this.logger.info("The preference kernel tells us that it cannot produce reliable information yet. Choosing one action at random.");
			return this.randomPolicy.getAction(node, actions);
		}

		/* determine probability to proceed */
		Map<A, Double> lastProbabilities = this.lastLocalProbabilityOfNode.computeIfAbsent(node, dummy -> new HashMap<>());
		int k = actions.size();
		double maxProb = lastProbabilities.isEmpty() ? 1.0 / k : lastProbabilities.values().stream().max(Double::compare).get();
		double probabilityToDerivePLModel = k * (1.0 - maxProb) / (k - 1);
		this.logger.info("Probability to derive a new model is {}*(1-{}) / ({} - 1) = {}. Last probs were: {}", k, maxProb, k, probabilityToDerivePLModel, lastProbabilities);

		/* make sure that actions are sorted */
		List<A> orderedActions = actions instanceof List ? (List<A>) actions : new ArrayList<>(actions);
		try {

			int numChildren = actions.size();

			/* compute gamma for this decision */
			IGammaFunction gammaFunction = this.getGammaFunction(node, actions);
			double relativeDepth = this.deepestRelativeNodeDepthsOfNodes.get(node);
			double nodeProbability = this.getProbabilityOfNode(node);
			double gammaValue = this.getGammaValue(node, actions);

			/* update node map (only if there are listeners for this) */
			if (this.hasListeners) {
				double lgw = ((CombinedGammaFunction) gammaFunction).getLongTermWeightBasedOnProbability(nodeProbability);
				nodeInfoMap.put("gamma", gammaValue);
				nodeInfoMap.put("longgammaweight", lgw);
				nodeInfoMap.put("prob", nodeProbability);
			}

			/* if the gamma value is 0, just return a random element */
			if (gammaValue == 0) {
				this.logger.info("Gamma is 0, so all options have equal probability of being chosen. Just return a random element.");
				nodeInfoMap.put("commitment", 0);
				this.eventBus.post(new NodePropertyChangedEvent<N>(null, node, nodeInfoMap));
				return SetUtil.getRandomElement(orderedActions, this.random);
			}

			/* check whether there is only one child */
			if (numChildren <= 1) {
				if (numChildren < 1) {
					throw new UnsupportedOperationException("Cannot compute action for nodes without successors.");
				}
				if (orderedActions.size() != 1) {
					throw new IllegalStateException();
				}
				nodeInfoMap.put("commitment", 1);
				this.eventBus.post(new NodePropertyChangedEvent<N>(null, node, nodeInfoMap));
				return orderedActions.iterator().next();
			}

			/* check whether epsilon forces us to explore */
			// if (this.random.nextDouble() < EPSILON * (1 - relativeDepth)) {
			// return this.randomPolicy.getAction(node, actions);
			// }

			/* estimate PL-parameters */
			Map<A, Double> skills;
			long skillEstimateStart = System.currentTimeMillis();
			long mmRuntime = 0;
			if (this.random.nextDouble() <= probabilityToDerivePLModel) {
				this.logger.debug("Computing PL-Problem instance");
				PLInferenceProblemEncoder encoder = new PLInferenceProblemEncoder();
				PLInferenceProblem problem = encoder.encode(this.preferenceKernel.getRankingsForActions(node, orderedActions));

				this.logger.debug("Start computation of skills for {}. Using {} rankings. Gamma value is {} based on node probability {} and depth {}", node, problem.getRankings().size(), gammaValue, nodeProbability, relativeDepth);
				skills = this.skillsForActions.get(node);
				long mmStart = System.currentTimeMillis();
				PLMMAlgorithm plAlgorithm = new PLMMAlgorithm(problem, skills != null ? new DoubleArrayList(orderedActions.stream().map(skills::get).collect(Collectors.toList())) : null, this.config);
				plAlgorithm.setLoggerName(this.getLoggerName() + ".pl");
				DoubleList skillVector = plAlgorithm.call();
				mmRuntime = System.currentTimeMillis() - mmStart;
				if (skillVector.size() != problem.getNumObjects()) {
					throw new IllegalStateException("Have " + skills.size() + " skills (" + skills + ") for " + problem.getNumObjects() + " objects.");
				}
				if (skills == null) {
					skills = new HashMap<>();
					this.skillsForActions.put(node, skills);
				}
				for (A action : orderedActions) {
					skills.put(action, skillVector.getDouble(encoder.getIndexOfObject(action)));
				}
			} else {
				skills = this.skillsForActions.get(node);
				this.logger.info("Reusing skill vetor of last iteration. Probability for reuse was {}. Skill map is: {}", 1 - probabilityToDerivePLModel, skills);
			}
			long skillEstimateRuntime = System.currentTimeMillis() - skillEstimateStart;
			Objects.requireNonNull(skills, "The skill map must not be null at this point.");

			/* adjust PL-parameters according to gamma */
			long choiceStart = System.currentTimeMillis();
			int n = skills.size();
			double sum = 0;
			Map<A, Double> gammaAdjustedSkills = new HashMap<>();
			for (Entry<A, Double> skillValue : skills.entrySet()) {
				double newVal = Math.pow(skillValue.getValue(), gammaValue);
				gammaAdjustedSkills.put(skillValue.getKey(), newVal);
				sum += newVal;
			}
			if (sum == 0) {
				throw new IllegalStateException();
			}

			/* compute probability vector */
			DoubleList pVector = new DoubleArrayList();
			double curMaxProb = 0;
			for (A action : orderedActions) {
				double newProbOfAction = gammaAdjustedSkills.get(action) / sum;
				curMaxProb = Math.max(curMaxProb, newProbOfAction);
				if (Double.isNaN(newProbOfAction)) {
					this.logger.error("Probability of successor is NaN! Skill vector: {}", skills);
				}
				lastProbabilities.put(action, newProbOfAction);
				pVector.add(newProbOfAction);
			}

			double commitment = 1 - (k * (1.0 - curMaxProb) / (k - 1));
			if (depth == 1) {
				for (Entry<A, DoubleList> entry : ((BootstrappingPreferenceKernel<N, A>) this.preferenceKernel).getObservations(node).entrySet()) {
					DescriptiveStatistics stats = new DescriptiveStatistics();
					for (double v : entry.getValue()) {
						stats.addValue(v);
					}
				}
			}
			nodeInfoMap.put("commitment", commitment);
			this.eventBus.post(new NodePropertyChangedEvent<N>(null, node, nodeInfoMap));

			/* draw random action */
			A randomChoice = SetUtil.getRandomElement(orderedActions, this.random, pVector);
			int chosenIndex = orderedActions.indexOf(randomChoice);
			double probOfChosenAction = pVector.getDouble(chosenIndex);
			long choiceRuntime = System.currentTimeMillis() - choiceStart;
			long runtime = System.currentTimeMillis() - start;

			/* update sequence of sure actions */
			long metaStart = System.currentTimeMillis();
			if (gammaValue >= 1) {
				Pair<A, Integer> lastChosenAction = this.sequentialCertaintyCounts.get(node);
				if (lastChosenAction != null) {
					if (lastChosenAction.getX().equals(randomChoice) && probOfChosenAction >= .99
							&& (!this.activeTree.hasItem(node) || this.activeTree.getRoot() == node || this.fixedDecisions.containsKey(this.activeTree.getPredecessors(node).iterator().next()))) {
						this.logger.info("Incrementing number of sure choices for this action by 1.");
						int numberOfSuccessfulChoices = lastChosenAction.getY();
						if (numberOfSuccessfulChoices >= MINIMUMNUMBERTOCOMMIT) {
							this.sequentialCertaintyCounts.remove(node);
							this.logger.warn("Definitely committing to action {} in node {} in depth {}. Freeing resources.", randomChoice, node, this.fixedDecisions.size());
							this.fixedDecisions.put(node, randomChoice);
							this.preferenceKernel.clearKnowledge(node);
							List<N> irrelevantNodes = new ArrayList<>();
							irrelevantNodes.addAll(this.activeTree.getSiblings(node));
							List<N> descendants = new ArrayList<>();
							irrelevantNodes.forEach(ni -> descendants.addAll(this.activeTree.getDescendants(ni)));
							irrelevantNodes.addAll(descendants);
							irrelevantNodes.forEach(in -> {
								this.preferenceKernel.clearKnowledge(in);
								this.activeTree.removeItem(in);
								this.eventBus.post(new NodeRemovedEvent<>(null, in));
							});
						} else {
							this.sequentialCertaintyCounts.put(node, new Pair<>(randomChoice, numberOfSuccessfulChoices + 1));
						}
					} else {
						this.logger.info("Resetting certainty count for this node.");
						this.sequentialCertaintyCounts.put(node, null);
					}
				} else if (probOfChosenAction >= .99) {
					this.logger.info("Initializing number of sure choices for this action by 1.");
					this.sequentialCertaintyCounts.put(node, new Pair<>(randomChoice, 1));
				}
			}

			/* log decision summary */
			if (this.logger.isDebugEnabled()) {
				for (int i = 0; i < n; i++) {
					A action = orderedActions.get(i);
					this.logger.debug("Derived probability of {}-th action {} by {} -> {} -> {}", i, action, skills.get(action), gammaAdjustedSkills.get(action), pVector.getDouble(i));
				}
			}
			long metaRuntime = System.currentTimeMillis() - metaStart;
			if (this.logger.isInfoEnabled()) {
				this.logger.info("Eventual choice after {}ms: {} (index {} with probability of {}%). Runtimes are as follows. PL: {}ms ({}ms for MM algorithm), Choice: {}ms, Meta: {}ms", runtime, randomChoice, chosenIndex,
						MathExt.round(100 * probOfChosenAction, 2), skillEstimateRuntime, mmRuntime, choiceRuntime, metaRuntime);
			}
			if (runtime > 10) {
				this.logger.warn("PL inference took {}ms for {} options, which is more than the allowed!", runtime, actions.size());
			}
			return randomChoice;
		} catch (AlgorithmTimeoutedException | AlgorithmExecutionCanceledException | AlgorithmException e) {
			throw new ActionPredictionFailedException(e);
		} catch (InterruptedException e) {
			this.logger.info("Policy thread has been interrupted. Re-interrupting thread, because no InterruptedException can be thrown here.");
			Thread.currentThread().interrupt();
			return null;
		}
	}

	public double getProbabilityOfNode(final N node) {
		N curNode = node;
		double prob = 1;
		N root = this.activeTree.getRoot();
		while (root != null && curNode != root) {
			N parent = this.activeTree.getPredecessors(curNode).iterator().next();
			A edge = this.activeTree.getEdgeLabel(parent, curNode);
			if (this.lastLocalProbabilityOfNode.containsKey(parent) && this.lastLocalProbabilityOfNode.get(parent).containsKey(edge)) {
				prob *= this.lastLocalProbabilityOfNode.get(parent).get(edge);
			} else {
				double uniform = 1.0 / this.activeTree.getSuccessors(parent).size();
				this.logger.debug("No probability known for node {}. Assuming uniform probability {}.", curNode, uniform);
				prob *= uniform;
			}
			curNode = parent;
		}
		return prob;
	}

	@Override
	public void registerListener(final Object listener) {
		this.eventBus.register(listener);
		this.hasListeners = true;
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
	public void updatePath(final ILabeledPath<N, A> path, final List<Double> scores) {
		double playoutScore = scores.stream().reduce((a, b) -> a + b).get(); // we neither discount nor care for the segmentation of the scores
		this.logger.info("Received a playout score of {}. Communicating this to the preference kernel.", playoutScore);
		this.preferenceKernel.signalNewScore(path, playoutScore);
		int depth = 0;
		List<N> nodes = path.getNodes();
		List<A> arcs = path.getArcs();
		int totalDepth = path.getNumberOfNodes();
		int pathLength = path.getNumberOfNodes();
		N lastNode = null;
		A lastAction = null;
		boolean isNextNodeRelevantForDecisions = true; // a node is relevant if its parent has been subject to decisions at least once
		while (isNextNodeRelevantForDecisions && depth < pathLength - 1) {
			N node = nodes.get(depth);
			A arc = arcs.get(depth);
			double relativeDepth = depth * 1.0 / totalDepth;
			if (lastNode != null) {
				this.activeTree.addEdge(lastNode, node, lastAction);
			}
			this.numPulls.computeIfAbsent(node, dummy -> new HashMap<>()).put(arc, this.numPulls.get(node).computeIfAbsent(arc, a -> 0) + 1);
			this.logger.debug("Updating action {} for node {} with score {} and incrementing its number of pulls.", arc, node, playoutScore);
			if (!this.deepestRelativeNodeDepthsOfNodes.containsKey(node) || this.deepestRelativeNodeDepthsOfNodes.get(node) < relativeDepth) {
				this.deepestRelativeNodeDepthsOfNodes.put(node, relativeDepth);
			}
			this.depths.put(node, depth);
			isNextNodeRelevantForDecisions = this.nodesForWhichAnActionHasBeenRequested.contains(node);
			lastNode = node;
			lastAction = arc;
			depth++;
		}

		/* update avg depth */
		this.avgDepth = (this.numUpdates * this.avgDepth + pathLength) / (this.numUpdates + 1.0);
		this.avgTargetDepth = this.avgDepth * GOAL_COMMIT_DEPTH;
		this.numUpdates++;
		if (!this.maxChildren.isEmpty()) {
			this.avgBranchingFactor = this.maxChildren.values().stream().reduce((a, b) -> a + b).get() / (1.0 * this.maxChildren.size());
		}
	}

	@Override
	public void setEstimatedNumberOfRemainingRollouts(final int pNumRollouts) {
		int numRollouts = pNumRollouts;
		if (pNumRollouts < 0) {
			this.logger.warn("Estimated number of remaining rollouts must not be negative but was {}! Setting it to 1.", pNumRollouts);
			numRollouts = 1;
		}
		this.expectedNumberOfTotalRollouts = this.numUpdates + numRollouts;
		if (this.avgBranchingFactor > 0) {
			double sum = 0;
			for (int h = this.fixedDecisions.size(); h < this.avgTargetDepth; h++) {
				sum += Math.pow(this.avgTargetDepth - h, GAMMA_LONG_DERIVATIVE_EXP);
			}
			if (sum < 0) {
				throw new IllegalStateException("Sum must not be negative!");
			}
			this.estimateForSlopeForTargetValueOfGammaEqualsOne = numRollouts / (this.avgBranchingFactor * sum);
			if (this.estimateForSlopeForTargetValueOfGammaEqualsOne < 0) {
				throw new IllegalStateException("The estimated slope is negative!");
			}
			if (!this.isApproximationCorrect(numRollouts)) {
				throw new IllegalStateException("The approximation for the slope relevant to compute gamma is errorneous.");
			}
		}
	}

	/**
	 *
	 * @param depth
	 *            is usually an int, but we allow double for theoretical reasons and to check correctness
	 * @return
	 */
	public double getNumberOfSamplesPerChildForGamma(final double depth) {
		if (depth > this.avgTargetDepth) {
			throw new IllegalArgumentException("Depth cannot be greater than avg depth.");
		}
		return Math.pow(this.avgTargetDepth - depth, GAMMA_LONG_DERIVATIVE_EXP) * this.estimateForSlopeForTargetValueOfGammaEqualsOne;
	}

	private boolean isApproximationCorrect(final int expectedNumberOfRemainingRollouts) {
		double sum = 0;
		if (this.fixedDecisions.size() > this.avgTargetDepth) {
			return true;
		}
		for (int h = this.fixedDecisions.size(); h < this.avgTargetDepth; h++) {
			double v = this.getNumberOfSamplesPerChildForGamma(h);
			if (v < 0) {
				throw new IllegalStateException("Observed a negativ value for number of samples per child: " + v);
			}
			sum += v * this.avgBranchingFactor;
		}
		int seen = (int) Math.round(sum);
		if (seen != expectedNumberOfRemainingRollouts) {
			this.logger.warn("Expected {} but saw {}", expectedNumberOfRemainingRollouts, seen);
		}
		return seen == expectedNumberOfRemainingRollouts;
	}

	public IPreferenceKernel<N, A> getPreferenceKernel() {
		return this.preferenceKernel;
	}

	public Set<N> getNodesForWhichAnActionHasBeenRequested() {
		return this.nodesForWhichAnActionHasBeenRequested;
	}

	public LabeledGraph<N, A> getActiveTree() {
		return this.activeTree;
	}

	public int getDepthOfActiveNode(final N node) {
		return this.depths.get(node);
	}
}

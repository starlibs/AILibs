package ai.libs.jaicore.search.algorithms.standard.mcts.comparison;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.eventbus.EventBus;

import ai.libs.jaicore.basic.events.IEventEmitter;
import ai.libs.jaicore.graph.Graph;
import ai.libs.jaicore.graph.LabeledGraph;
import ai.libs.jaicore.search.algorithms.standard.mcts.ActionPredictionFailedException;
import ai.libs.jaicore.search.algorithms.standard.mcts.IPathUpdatablePolicy;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.observationfilter.EmptyObservationFilter;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.wincomputer.BestAndLatestVsBestAndLatestWinComputer;

/**
 * This is a slightly simplified implementation of the algorithm presented in
 *
 *
 * @author felix
 *
 * @param <N>
 * @param <A>
 */
public class BradleyTerryLikelihoodPolicy<N, A> implements IPathUpdatablePolicy<N, A, Double>, ILoggingCustomizable, IEventEmitter {

	private EventBus eventBus = new EventBus();
	private Logger logger = LoggerFactory.getLogger(BradleyTerryLikelihoodPolicy.class);

	public class BTModel<T> {
		private final T node;

		public BTModel(final T node, final BTModel parent, final int depth) {
			super();
			this.node = node;
			this.depth = depth;
		}

		public final int depth;
		public int maxObservedDepthUnderNode = -1;
		int visits = 0;
		public BTModel parent;
		public BTModel left;
		public BTModel right;

		public final MinMaxPriorityQueue<Double> observedScoresLeft = MinMaxPriorityQueue.create();
		public final MinMaxPriorityQueue<Double> observedScoresRight = MinMaxPriorityQueue.create();

		private int winsLeft;
		private int winsRight;

		private double pLeft = .5;
		private double pRight = .5;

		public void addScore(final double score, final boolean isForRightChild) {
			boolean observationsRemoved = false;

			/* update observations */
			observationsRemoved = BradleyTerryLikelihoodPolicy.this.updater.updateObservations(this, score, isForRightChild);

			/* now update wins */
			int winsLeftBefore = this.winsLeft;
			int winsRightBefore = this.winsRight;
			BradleyTerryLikelihoodPolicy.this.winComputer.updateWinsOfChildrenBasedOnNewScore(this, score, isForRightChild);

			/* now update probabilities */
			BradleyTerryLikelihoodPolicy.this.logger.trace("Updated number of wins from {}/{} to {}/{}", winsLeftBefore, winsRightBefore, this.winsLeft, this.winsRight);
			double pLeftBefore = this.pLeft;
			double pRightBefore = this.pRight;
			if (this.winsLeft > 0 || this.winsRight > 0) {
				this.updateProbabilities();
				BradleyTerryLikelihoodPolicy.this.logger.trace("Observation of {} on {} causes probabilities to update from {}/{} to {}/{}", score, isForRightChild ? "right" : "left", pLeftBefore, pRightBefore, this.pLeft, this.pRight);
				pLeftBefore = this.pLeft;
				pRightBefore = this.pRight;
				this.scaleProbabilities();
				BradleyTerryLikelihoodPolicy.this.logger.trace("Scaling according to {} visits on {} causes probabilities to update from {}/{} to {}/{}", this.visits, this.node, pLeftBefore, pRightBefore, this.pLeft, this.pRight);
			}
			else {
				if (!this.observedScoresLeft.isEmpty() && !this.observedScoresRight.isEmpty() && this.observedScoresLeft.equals(this.observedScoresRight)) {
					throw new IllegalStateException("With at least one observation on each side for node " + this.node + ", there should be a decision unless both have the same score. Scores are: \n\tleft: " + this.observedScoresLeft + "\n\tright: " + this.observedScoresRight);
				}
				BradleyTerryLikelihoodPolicy.this.logger.debug("Did not update the probability model of node {} yet, because for one of the nodes, no observation has been made yet. Current number of observations: {}/{}", this.node, this.observedScoresLeft.size(), this.observedScoresRight.size());
			}
			BradleyTerryLikelihoodPolicy.this.eventBus.post(new ObservationsUpdatedEvent<T>(this.getClass().getName(), this.node, this.visits, this.observedScoresLeft, this.observedScoresRight, this.winsLeft, this.winsRight, pLeftBefore, pRightBefore, this.pLeft, this.pRight));
			//			if (this.depth == 30) {
			//				System.out.println(this.observedScoresLeft + " vs " + this.observedScoresRight + " -> (" + this.winsLeft + ", " + this.winsRight + ")");
			//			}

		}

		private void updateProbabilities() {
			double pLeftNew = this.getUpdatedProbability(false);
			double pRightNew = this.getUpdatedProbability(true);
			double sum = pLeftNew + pRightNew;
			this.pLeft = pLeftNew / sum;
			this.pRight = pRightNew / sum;
		}

		private void scaleProbabilities() {
			final double MAX_EXPONENT = 1000;
			//			double exp = Math.min(MAX_EXPONENT, new AffineFunction(0, 0, Math.pow(BradleyTerryLikelihoodPolicy.this.maxIter, 2), MAX_EXPONENT).apply(Math.pow(BradleyTerryLikelihoodPolicy.this.numberOfEvaluations, 2))); // squared increase
			//			double exp = new AffineFunction(0, 0, BradleyTerryLikelihoodPolicy.this.maxIter, MAX_EXPONENT).apply(BradleyTerryLikelihoodPolicy.this.numberOfEvaluations); // linear increase
			Double gammaScore = Double.valueOf(BradleyTerryLikelihoodPolicy.this.gamma.apply(this));
			if (gammaScore.equals(Double.NaN)) {
				throw new IllegalStateException("Computing gamma failed.");
			}
			double pLeftNew = Math.pow(this.pLeft, gammaScore);
			double pRightNew = Math.pow(this.pRight, gammaScore);
			double sum = pLeftNew + pRightNew;
			this.pLeft = pLeftNew / sum;
			this.pRight = pRightNew / sum;
			BradleyTerryLikelihoodPolicy.this.logger.debug("Scaling with gamma value {} after {} visits. New probabilities: {}/{}", gammaScore, this.visits, this.pLeft, this.pRight);

			/* quick check about where best performances are observed */
			if (gammaScore != 0) {
				boolean bestScoreIsLeft = this.observedScoresRight.isEmpty() || this.observedScoresLeft.peek() < this.observedScoresRight.peek();
				double gapBetweenLeftAndRight = Math.abs(this.observedScoresLeft.peek() - this.observedScoresRight.peek());
				double gapBetweenLeftAndRightProbability = Math.abs(this.pLeft - this.pRight);
				if (gapBetweenLeftAndRight > 0.05 && gapBetweenLeftAndRightProbability > 0.2 && (bestScoreIsLeft && this.pRight > this.pLeft || !bestScoreIsLeft && this.pRight < this.pLeft)) {
					BradleyTerryLikelihoodPolicy.this.logger.warn("Best choice is in the branch with less probability! Best values: {}/{}. Probabilities: {}/{}. Depth: {}, Number of visits: {}", this.observedScoresLeft.peek(),this.observedScoresRight.peek(), this.pLeft, this.pRight, this.depth, this.visits);
				}
			}
		}

		private double getUpdatedProbability(final boolean right) {
			double factor = (this.pLeft + this.pRight) / (this.winsLeft + this.winsRight);
			return factor * (right ? this.winsRight : this.winsLeft);
		}

		public int getWinsLeft() {
			return this.winsLeft;
		}

		public void setWinsLeft(final int winsLeft) {
			this.winsLeft = winsLeft;
		}

		public int getWinsRight() {
			return this.winsRight;
		}

		public void setWinsRight(final int winsRight) {
			this.winsRight = winsRight;
		}

		public double getPathProbability() {
			if (this.parent == null) {
				return 1.0;
			}
			BTModel parentModel = this.parent;
			double parentProbabilty = this.parent.getPathProbability();
			if (parentModel.left != this && parentModel.right != this) {
				throw new IllegalStateException();
			}
			return parentProbabilty * (parentModel.left == this ? parentModel.pLeft : parentModel.pRight);
		}

		public int getVisits() {
			return this.visits;
		}
	}

	private final Map<N, BTModel> nodeModels = new HashMap<>();
	private LabeledGraph<N, A> explorationGraph;
	private Map<N, Integer> depthMap = new HashMap<>();
	private Map<N, Integer> maxObservedDepthUnderNode = new HashMap<>();

	private final int maxIter;
	private int numberOfEvaluations = 0;
	private final Random random;
	private final IWinComputer winComputer = new BestAndLatestVsBestAndLatestWinComputer();
	private final IObservationUpdate updater = new EmptyObservationFilter();
	private final IGammaFunction gamma = new GammaFunction();

	public BradleyTerryLikelihoodPolicy(final int maxIter, final Random random) {
		super();
		this.maxIter = maxIter;
		this.random = random;
	}

	@Override
	public A getAction(final N node, final Map<A, N> actionsWithSuccessors) throws ActionPredictionFailedException {
		BTModel nodeModel = this.nodeModels.get(node);
		if (nodeModel == null) {
			throw new IllegalArgumentException("Cannot derive any action with tree policy for node without a node model: " + node);
		}
		if (actionsWithSuccessors == null || actionsWithSuccessors.isEmpty()) {
			throw new IllegalArgumentException("No actions and successors are provided.");
		}
		A leftAction = null;
		A rightAction = null;
		if (actionsWithSuccessors.size() == 1) {
			A action = actionsWithSuccessors.keySet().iterator().next();
			this.logger.info("Recommending only available action {}", action);
			return action;
		}

		//		if (nodeModel.getPathProbability() < .01) {
		//			System.out.println(nodeModel.visits + ": " + nodeModel.pLeft + ", " +nodeModel.pRight + ": " + nodeModel.observedScoresLeft + ", " + nodeModel.observedScoresRight);
		//		}

		/* determine which is the left and which is the right action */
		this.logger.debug("Selecting action among {} candidates: {}", actionsWithSuccessors.size(), actionsWithSuccessors.keySet());
		for (Entry<A, N> entry : actionsWithSuccessors.entrySet()) {
			N successor = entry.getValue();
			if (successor == null) {
				throw new IllegalArgumentException();
			}
			if (nodeModel.left == null || nodeModel.right == null) {
				throw new IllegalStateException("Tree policy is asked for a decision in node " + node + ", whose model does not have a value for left or right child. Scores are " + nodeModel.pLeft + "/" + nodeModel.pRight);
			}
			if (nodeModel.left.node.equals(successor)) {
				leftAction = entry.getKey();
			}
			else if (nodeModel.right.node.equals(successor)) {
				rightAction = entry.getKey();
			}
			else {
				throw new IllegalStateException();
			}
		}
		if (leftAction == null || rightAction == null) {
			throw new IllegalStateException("One of the actions is null: " + leftAction + " (left), " + rightAction + " (right)");
		}

		/* if the numbers of actions is not enough, choose the one with less examples */
		//		int numOfRequiredObservations = this.requiredNumberOfObservationsForEach.apply(node);
		A action;
		//		if (Math.min(nodeModel.observedScoresLeft.size(), nodeModel.observedScoresRight.size()) < numOfRequiredObservations) {
		//			action = nodeModel.observedScoresLeft.size() <= nodeModel.observedScoresRight.size() ? leftAction : rightAction;
		//			this.logger.info("Refusing opinion, because number of min samples does not exceed {}. Choosing action {} to balance number of observations", numOfRequiredObservations, action);
		//		}
		//		else {
		assert Math.abs(1 - (nodeModel.pLeft + nodeModel.pRight)) < 0.000001 : "Sum of probabilities is " + nodeModel.pLeft + " + " + nodeModel.pRight + " = " + (nodeModel.pLeft + nodeModel.pRight);
		if (this.random == null) {
			action = nodeModel.pLeft > nodeModel.pRight ? leftAction : rightAction;
			this.logger.info("Deterministically recommending action {} based on probabilities {}/{}", action, nodeModel.pLeft, nodeModel.pRight);
		}
		else {
			action = this.random.nextDouble() <= nodeModel.pLeft ? leftAction : rightAction;
			//			System.out.println(nodeModel.depth + " (" + nodeModel.visits + " visits): " + nodeModel.pLeft + ", " + nodeModel.pRight + " -> " + action);
			this.logger.info("Stochastically recommending action {} where probability of action {} was {} and probability of action {} was {}", action, leftAction, nodeModel.pLeft, rightAction, nodeModel.pRight);
		}
		//		}
		return action;
	}

	@Override
	public void updatePath(final List<N> path, final Double playoutScore) {
		this.logger.info("Updating path {} with score {}", path, playoutScore);
		this.numberOfEvaluations ++;
		BTModel last = null;
		int n = path.size();
		for (int i = 0; i < n; i++) {
			int depth = n - 1 - i;
			N node = path.get(depth);
			BTModel modelOfCurrent = this.nodeModels.computeIfAbsent(node, k -> new BTModel(k, null, depth));
			if (last != null) {
				last.parent = modelOfCurrent;
			}
			modelOfCurrent.maxObservedDepthUnderNode = Math.max(i, modelOfCurrent.maxObservedDepthUnderNode);
			this.maxObservedDepthUnderNode.put(node, modelOfCurrent.maxObservedDepthUnderNode);
			this.logger.trace("Max depth under node {} is {}", node, this.maxObservedDepthUnderNode.get(node));
			if (!this.depthMap.containsKey(node)) {
				this.depthMap.put(node, depth);
			}
			assert this.depthMap.get(node) == depth : "Depth should be " + this.depthMap.get(node) + " but is " + i;
			if (last != null) {
				modelOfCurrent.visits ++;
				if (modelOfCurrent.left == null || modelOfCurrent.left.equals(last)) {
					if (modelOfCurrent.left == null) {
						modelOfCurrent.left = last;
					}
					modelOfCurrent.addScore(playoutScore, false);
					this.logger.debug("Updated left score of node {} with {}", node, playoutScore);
				}
				else if (modelOfCurrent.right == null || modelOfCurrent.right.equals(last)) {
					if (modelOfCurrent.right == null) {
						modelOfCurrent.right = last;
					}
					modelOfCurrent.addScore(playoutScore, true);
					this.logger.debug("Updated right score of node {} with {}", node, playoutScore);
				}
				else {
					throw new IllegalStateException();
				}
			}
			last = modelOfCurrent;
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

	public Graph<N> getExplorationGraph() {
		return this.explorationGraph;
	}

	public void setExplorationGraph(final LabeledGraph<N, A> explorationGraph) {
		this.explorationGraph = explorationGraph;
	}

	@Override
	public void registerListener(final Object listener) {
		this.eventBus.register(listener);
	}


}

package ai.libs.jaicore.search.algorithms.mdp.mcts;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.basic.algorithm.AAlgorithm;
import ai.libs.jaicore.search.algorithms.standard.mcts.ActionPredictionFailedException;
import ai.libs.jaicore.search.algorithms.standard.mcts.IPathUpdatablePolicy;
import ai.libs.jaicore.search.algorithms.standard.mcts.IPolicy;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.IMDP;
import ai.libs.jaicore.search.probleminputs.MDPUtils;

public class MCTSPolicySearch<N, A> extends AAlgorithm<IMDP<N, A, Double>, IPolicy<N, A>> {

	private final IMDP<N, A, Double> mdp;
	private final double gamma;
	private final double epsilon;
	private final int maxDepth;
	private final MDPUtils utils = new MDPUtils();
	private final IPathUpdatablePolicy<N, A, Double> treePolicy;
	private final IPolicy<N, A> defaultPolicy;
	private final double maxIterations;
	private int iterations = 0;
	private Collection<N> tpReadyStates = new HashSet<>();
	private Map<N, Queue<A>> untriedActionsOfIncompleteStates = new HashMap<>();

	public MCTSPolicySearch(final IMDP<N, A, Double> input, final IPathUpdatablePolicy<N, A, Double> treePolicy, final IPolicy<N, A> defaultPolicy, final double maxIterations, final double gamma, final double epsilon) {
		super(input);
		Objects.requireNonNull(input);
		Objects.requireNonNull(treePolicy);
		Objects.requireNonNull(defaultPolicy);
		this.mdp = input;
		this.treePolicy = treePolicy;
		this.defaultPolicy = defaultPolicy;
		this.maxIterations = maxIterations;
		this.gamma = gamma;
		this.epsilon = epsilon;
		this.maxDepth = (int)Math.ceil(Math.log(epsilon) / Math.log(gamma));
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		switch (this.getState()) {
		case CREATED:
			return this.activate();
		case ACTIVE:
			if (this.iterations >= this.maxIterations) {
				return this.terminate();
			}
			else {
				try {

					this.iterations ++;

					/* draw playout */
					this.logger.debug("Draw next playout.");
					double score = 0;
					ILabeledPath<N, A> path = new SearchGraphPath<>(this.mdp.getInitState());
					N current = path.getRoot();
					A action = null;
					int phase = 1;
					double discount = 1.0;
					Collection<A> possibleActions;
					while (path.getNumberOfNodes() < this.maxDepth) {

						/* determine possible actions */
						possibleActions = this.mdp.getApplicableActions(current);
						if (possibleActions.isEmpty()) {
							break;
						}

						/* first case */
						if (phase == 1 && this.tpReadyStates.contains(current)) {
							this.logger.debug("Ask tree policy to choose one action of: {}.", possibleActions);
							action = this.treePolicy.getAction(current, possibleActions);
							this.logger.debug("Tree policy recommended action {}.", action);
						}
						else {
							if (phase == 1) { // switch to next phase
								phase = 2;
							}
							if (phase == 2) {
								Queue<A> untriedActions;
								if (!this.untriedActionsOfIncompleteStates.containsKey(current)) {
									untriedActions = new LinkedList<>(possibleActions);
									if (untriedActions.isEmpty()) {
										this.treePolicy.updatePath(path, score);
										IAlgorithmEvent event = new MCTSIterationCompletedEvent<>(this, this.treePolicy, new EvaluatedSearchGraphPath<>(path, score));
										this.post(event);
										return event;
									}
									this.untriedActionsOfIncompleteStates.put(current, new LinkedList<>(possibleActions));
								}
								else {
									untriedActions = this.untriedActionsOfIncompleteStates.get(current);
								}
								assert !untriedActions.isEmpty() : "Untried actions must not be empty!";
								action = untriedActions.poll();
								if (untriedActions.isEmpty()) { // if this was the last untried action, remove it from that field and add it to the tree policy pool
									this.untriedActionsOfIncompleteStates.remove(current);
									this.tpReadyStates.add(current);
									this.logger.debug("Adding state {} to tree policy domain.", current);
								}
								phase = 3;
							}
							else if (phase == 3) {
								this.logger.debug("Ask default policy to choose one action of: {}.", possibleActions);
								action = this.defaultPolicy.getAction(current, possibleActions);
								assert possibleActions.contains(action);
								this.logger.debug("Default policy chose action {}.", action);
							}
						}

						N nextState = this.utils.drawSuccessorState(this.mdp, current, action);
						score += discount * this.mdp.getScore(current, action, nextState);
						discount *= this.gamma;
						current = nextState;
						path.extend(current, action);
					}

					/* update tree policy with accumulated score */
					this.logger.debug("Propagating score {} over the path with actions {} and final state {}.", score, path.getArcs(), path.getHead());
					this.treePolicy.updatePath(path, score);
					IAlgorithmEvent event = new MCTSIterationCompletedEvent<>(this, this.treePolicy, new EvaluatedSearchGraphPath<>(path, score));
					this.post(event);
					return event;
				}
				catch (ActionPredictionFailedException e) {
					throw new AlgorithmException("Could not create playout!", e);
				}
			}
		default:
			throw new IllegalStateException("Don't know what to do in state " + this.getState());
		}
	}

	public int getIterations() {
		return this.iterations;
	}

	public IPathUpdatablePolicy<N, A, Double> getTreePolicy() {
		return this.treePolicy;
	}

	@Override
	public IPolicy<N, A> call() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		while (this.hasNext()) {
			this.nextWithException();
		}
		return this.treePolicy;
	}

	@Override
	public void setLoggerName(final String name) {
		super.setLoggerName(name);
		if (this.treePolicy instanceof ILoggingCustomizable) {
			((ILoggingCustomizable)this.treePolicy).setLoggerName(name + ".tp");
		}
		this.utils.setLoggerName(name + ".utils");
	}
}

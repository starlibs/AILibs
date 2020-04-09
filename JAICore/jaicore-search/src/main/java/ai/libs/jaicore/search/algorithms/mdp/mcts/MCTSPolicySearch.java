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
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.IMDP;
import ai.libs.jaicore.search.probleminputs.MDPUtils;

public class MCTSPolicySearch<N, A> extends AAlgorithm<IMDP<N, A, Double>, IPolicy<N, A>> {

	private final IMDP<N, A, Double> mdp;
	private final MDPUtils utils = new MDPUtils();
	private final IPathUpdatablePolicy<N, A, Double> treePolicy;
	private final IPolicy<N, A> defaultPolicy;
	private final double maxIterations;
	private int iterations = 0;
	private Collection<N> tpReadyStates = new HashSet<>();
	private Map<N, Queue<A>> untriedActionsOfIncompleteStates = new HashMap<>();

	public MCTSPolicySearch(final IMDP<N, A, Double> input, final IPathUpdatablePolicy<N, A, Double> treePolicy, final IPolicy<N, A> defaultPolicy, final double maxIterations) {
		super(input);
		Objects.requireNonNull(input);
		Objects.requireNonNull(treePolicy);
		Objects.requireNonNull(defaultPolicy);
		this.mdp = input;
		this.treePolicy = treePolicy;
		this.defaultPolicy = defaultPolicy;
		this.maxIterations = maxIterations;
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
					while (this.tpReadyStates.contains(current)) {
						this.logger.debug("Ask tree policy to choose one action of: {}.", this.mdp.getApplicableActions(current));
						A action = this.treePolicy.getAction(current, this.mdp.getApplicableActions(current));
						this.logger.debug("Tree policy recommended action {}.", action);
						N nextState = this.utils.drawSuccessorState(this.mdp, current, action);
						score += this.mdp.getScore(current, action, nextState);
						current = nextState;
						path.extend(current, action);
					}
					Queue<A> untriedActions;
					if (!this.untriedActionsOfIncompleteStates.containsKey(current)) {
						untriedActions = new LinkedList<>(this.mdp.getApplicableActions(current));
						if (untriedActions.isEmpty()) {
							this.treePolicy.updatePath(path, score);
							return null;
						}
						this.untriedActionsOfIncompleteStates.put(current, new LinkedList<>(this.mdp.getApplicableActions(current)));
					}
					else {
						untriedActions = this.untriedActionsOfIncompleteStates.get(current);
					}
					assert !untriedActions.isEmpty() : "Untried actions must not be empty!";
					A nextAction = untriedActions.poll();
					if (untriedActions.isEmpty()) { // if this was the last untried action, remove it from that field and add it to the tree policy pool
						this.untriedActionsOfIncompleteStates.remove(current);
						this.tpReadyStates.add(current);
						this.logger.debug("Adding state {} to tree policy domain.", current);
					}
					N nextState = this.utils.drawSuccessorState(this.mdp, current, nextAction);
					score += this.mdp.getScore(current, nextAction, nextState);
					current = nextState;
					path.extend(current, nextAction);
					Collection<A> possibleActions = this.mdp.getApplicableActions(current);
					while (!possibleActions.isEmpty()) {
						this.logger.debug("Ask default policy to choose one action of: {}.", possibleActions);
						A action = this.defaultPolicy.getAction(current, possibleActions);
						assert possibleActions.contains(action);
						this.logger.debug("Default policy chose action {}.", action);
						nextState = this.utils.drawSuccessorState(this.mdp, current, action);
						score += this.mdp.getScore(current, action, nextState);
						current = nextState;
						path.extend(current, action);
						possibleActions = this.mdp.getApplicableActions(current);
					}

					/* update tree policy with accumulated score */
					this.logger.debug("Propagating score {} over the path with actions {} and final state {}.", score, path.getArcs(), path.getHead());
					this.treePolicy.updatePath(path, score);
				}
				catch (ActionPredictionFailedException e) {
					throw new AlgorithmException("Could not create playout!", e);
				}
				return null;
			}
		default:
			throw new IllegalStateException("Don't know what to do in state " + this.getState());
		}
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
		((ILoggingCustomizable)this.treePolicy).setLoggerName(name + ".tp");
		this.utils.setLoggerName(name + ".utils");
	}
}

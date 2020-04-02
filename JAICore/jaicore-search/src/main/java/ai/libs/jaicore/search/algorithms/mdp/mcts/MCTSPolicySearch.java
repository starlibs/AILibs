package ai.libs.jaicore.search.algorithms.mdp.mcts;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
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
	private final IPathUpdatablePolicy<N, A, Double> treePolicy;
	private final IPolicy<N, A> defaultPolicy;
	private final int maxIterations;
	private int iterations = 0;
	private Collection<N> tpReadyStates = new HashSet<>();
	private Map<N, Collection<A>> untriedActionsOfIncompleteStates = new HashMap<>();

	public MCTSPolicySearch(final IMDP<N, A, Double> input, final IPathUpdatablePolicy<N, A, Double> treePolicy, final IPolicy<N, A> defaultPolicy, final int maxIterations) {
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
					double score = 0;
					ILabeledPath<N, A> path = new SearchGraphPath<>(this.mdp.getInitState());
					N current = path.getRoot();
					while (this.tpReadyStates.contains(current)) {
						A action = this.treePolicy.getAction(current, this.mdp.getApplicableActions(current));
						N nextState = MDPUtils.drawSuccessorState(this.mdp, current, action);
						score += this.mdp.getScore(current, action, nextState);
						current = nextState;
						path.extend(current, action);
					}
					Collection<A> untriedActions = this.untriedActionsOfIncompleteStates.computeIfAbsent(current, this.mdp::getApplicableActions);
					A nextAction = untriedActions.iterator().next();
					N nextState = MDPUtils.drawSuccessorState(this.mdp, current, nextAction);
					score += this.mdp.getScore(current, nextAction, nextState);
					current = nextState;
					path.extend(current, nextAction);
					Collection<A> possibleActions = this.mdp.getApplicableActions(current);
					while (!possibleActions.isEmpty()) {
						A action = this.defaultPolicy.getAction(current, possibleActions);
						nextState = MDPUtils.drawSuccessorState(this.mdp, current, action);
						score += this.mdp.getScore(current, action, nextState);
						current = nextState;
						path.extend(current, action);
					}
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
}

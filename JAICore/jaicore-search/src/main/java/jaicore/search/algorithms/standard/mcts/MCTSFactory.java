package jaicore.search.algorithms.standard.mcts;

import jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class MCTSFactory<N, A, V extends Comparable<V>> extends StandardORGraphSearchFactory<GraphSearchWithPathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>, N, A, V> implements IOptimalPathInORGraphSearchFactory<GraphSearchWithPathEvaluationsInput<N, A, V>, N, A, V> {
	private IPathUpdatablePolicy<N, A, V> treePolicy;
	private IPolicy<N, A, V> defaultPolicy;
	private V evaluationFailurePenalty;
	private boolean forbidDoublePaths;

	public IPathUpdatablePolicy<N, A, V> getTreePolicy() {
		return treePolicy;
	}

	public void setTreePolicy(IPathUpdatablePolicy<N, A, V> treePolicy) {
		this.treePolicy = treePolicy;
	}

	public IPolicy<N, A, V> getDefaultPolicy() {
		return defaultPolicy;
	}

	public void setDefaultPolicy(IPolicy<N, A, V> defaultPolicy) {
		this.defaultPolicy = defaultPolicy;
	}

	public V getEvaluationFailurePenalty() {
		return evaluationFailurePenalty;
	}

	public void setEvaluationFailurePenalty(V evaluationFailurePenalty) {
		this.evaluationFailurePenalty = evaluationFailurePenalty;
	}

	public boolean isForbidDoublePaths() {
		return forbidDoublePaths;
	}

	public void setForbidDoublePaths(boolean forbidDoublePaths) {
		this.forbidDoublePaths = forbidDoublePaths;
	}

	@Override
	public MCTS<N, A, V> getAlgorithm() {
		return new MCTS<>(getInput(), treePolicy, defaultPolicy, evaluationFailurePenalty, forbidDoublePaths);
	}
}

package ai.libs.jaicore.search.algorithms.standard.mcts;

import ai.libs.jaicore.graph.LabeledGraph;

public interface IGraphDependentPolicy<N, A> {
	public void setGraph(LabeledGraph<N, A> graph);
}

package ai.libs.jaicore.search.algorithms.mdp.mcts;

import ai.libs.jaicore.graph.LabeledGraph;

public interface IGraphDependentPolicy<N, A> {
	public void setGraph(LabeledGraph<N, A> graph);
}

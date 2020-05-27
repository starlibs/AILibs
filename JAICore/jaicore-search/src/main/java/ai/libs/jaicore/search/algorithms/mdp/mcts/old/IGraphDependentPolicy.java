package ai.libs.jaicore.search.algorithms.mdp.mcts.old;

import ai.libs.jaicore.graph.LabeledGraph;

public interface IGraphDependentPolicy<N, A> {
	public void setGraph(LabeledGraph<N, A> graph);
}

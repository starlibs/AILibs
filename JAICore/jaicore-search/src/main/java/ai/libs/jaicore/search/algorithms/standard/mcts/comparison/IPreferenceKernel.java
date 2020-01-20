package ai.libs.jaicore.search.algorithms.standard.mcts.comparison;

import java.util.List;

import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.graph.LabeledGraph;

public interface IPreferenceKernel<N, A> {

	public void setExplorationGraph(LabeledGraph<N, A> graph);

	public void signalNewScore(ILabeledPath<N, A> path, double score);

	public List<List<N>> getRankingsForChildrenOfNode(final N node);

	public boolean canProduceReliableRankings(final N node);
}

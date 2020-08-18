package ai.libs.jaicore.search.algorithms.mdp.mcts.comparison;

import java.util.Collection;
import java.util.List;

import org.api4.java.datastructure.graph.ILabeledPath;

public interface IPreferenceKernel<N, A> {

	public void signalNewScore(ILabeledPath<N, A> path, double score);

	public List<List<A>> getRankingsForActions(final N node, Collection<A> actions);

	public boolean canProduceReliableRankings(final N node, Collection<A> actions);

	public void clearKnowledge(N node);
}

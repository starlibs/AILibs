package ai.libs.jaicore.search.algorithms.mdp.mcts.comparison;

import java.util.Collection;
import java.util.List;

import org.api4.java.datastructure.graph.ILabeledPath;

public interface IPreferenceKernel<N, A> {

	public A getMostImportantActionToObtainApplicability(final N node, Collection<A> actions);

	public void signalNewScore(ILabeledPath<N, A> path, double score);

	public List<List<A>> getRankingsForActions(final N node, Collection<A> actions);

	public boolean canProduceReliableRankings(final N node, Collection<A> actions);

	public void signalNodeActiveness(N node); // signals that node is now in principle a candidate for the tree policy

	public void clearKnowledge(N node);

	public int getErasedObservationsInTotal(); // allows to ask how many observations have been eliminated
}

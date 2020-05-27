package ai.libs.jaicore.search.algorithms.mdp.mcts;

import org.api4.java.datastructure.graph.ILabeledPath;

public interface IPathLikelihoodProvidingPolicy<N, A> {
	public double getLikelihood(ILabeledPath<N, A> path);
}

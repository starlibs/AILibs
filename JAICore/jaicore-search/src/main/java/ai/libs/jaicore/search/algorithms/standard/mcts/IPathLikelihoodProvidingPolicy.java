package ai.libs.jaicore.search.algorithms.standard.mcts;

import org.api4.java.datastructure.graph.IPath;

public interface IPathLikelihoodProvidingPolicy<N, A> {
	public double getLikelihood(IPath<N, A> path);
}

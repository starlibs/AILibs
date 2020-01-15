package ai.libs.jaicore.search.algorithms.standard.mcts;

import org.api4.java.datastructure.graph.ILabeledPath;

public interface IPathLikelihoodProvidingPolicy<N, A> {
	public double getLikelihood(ILabeledPath<N, A> path);
}

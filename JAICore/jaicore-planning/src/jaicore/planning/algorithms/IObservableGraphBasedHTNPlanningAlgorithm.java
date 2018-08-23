package jaicore.planning.algorithms;

import jaicore.graph.IObservableGraphAlgorithm;
import jaicore.search.structure.core.GraphGenerator;

public interface IObservableGraphBasedHTNPlanningAlgorithm<R extends IPlanningSolution, N,A,V extends Comparable<V>> extends IHTNPlanningAlgorithm<R>, IObservableGraphAlgorithm<N, A>, IPathToPlanConverter<N> {
	public GraphGenerator<N, A> getGraphGenerator();
}

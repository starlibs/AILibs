package jaicore.planning.algorithms;

import jaicore.graph.IObservableGraphAlgorithm;

public interface IObservableGraphBasedHTNPlanningAlgorithm<R extends IPlanningSolution, N,A,V extends Comparable<V>> extends IHTNPlanningAlgorithm<R>, IObservableGraphAlgorithm<N, A>, IPathToPlanConverter<N> {
	
}

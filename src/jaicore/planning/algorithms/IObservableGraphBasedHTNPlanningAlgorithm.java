package jaicore.planning.algorithms;

import jaicore.graph.observation.IObservableGraphAlgorithm;

public interface IObservableGraphBasedHTNPlanningAlgorithm<N,A,V extends Comparable<V>> extends IHTNPlanningAlgorithm, IObservableGraphAlgorithm<N, A>, IPathToPlanConverter<N> {
	
}

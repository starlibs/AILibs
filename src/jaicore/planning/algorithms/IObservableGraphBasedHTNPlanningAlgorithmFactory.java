package jaicore.planning.algorithms;

import jaicore.planning.model.task.IHTNPlanningProblem;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearchFactory;
import jaicore.search.algorithms.standard.core.INodeEvaluator;

public interface IObservableGraphBasedHTNPlanningAlgorithmFactory<N, A, V extends Comparable<V>> {
	
	public IObservableGraphBasedHTNPlanningAlgorithm<N, A, V> newAlgorithm(IHTNPlanningProblem problem, IObservableORGraphSearchFactory<N, A, V> searchFactory, INodeEvaluator<N, V> nodeEvaluator, int numberOfCPUs);
	
	public IObservableGraphBasedHTNPlanningAlgorithm<N, A, V> newAlgorithm(IHTNPlanningProblem problem, int numberOfCPUs);
}

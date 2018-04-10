package jaicore.planning.algorithms;

import jaicore.planning.model.task.IHTNPlanningProblem;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearchFactory;
import jaicore.search.algorithms.standard.core.INodeEvaluator;

public interface IHTNPlanningAlgorithmFactory<N, A, V extends Comparable<V>> {
	
	public IHTNPlanningAlgorithm newAlgorithm(IHTNPlanningProblem problem, IObservableORGraphSearchFactory<N, A, V> searchFactory, INodeEvaluator<N, V> nodeEvaluator, int numberOfCPUs);
	
	public IHTNPlanningAlgorithm newAlgorithm(IHTNPlanningProblem problem, int numberOfCPUs);
}

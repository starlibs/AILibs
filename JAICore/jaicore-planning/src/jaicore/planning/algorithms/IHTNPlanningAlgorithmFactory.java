package jaicore.planning.algorithms;

import jaicore.planning.model.task.IHTNPlanningProblem;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearchFactory;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;

public interface IHTNPlanningAlgorithmFactory<N, A, V extends Comparable<V>,R extends IPlanningSolution> {
	
	public IHTNPlanningAlgorithm<R> newAlgorithm(IHTNPlanningProblem problem, IObservableORGraphSearchFactory<N, A, V> searchFactory, INodeEvaluator<N, V> nodeEvaluator, int numberOfCPUs);
	
	public IHTNPlanningAlgorithm<R> newAlgorithm(IHTNPlanningProblem problem, int numberOfCPUs);
}

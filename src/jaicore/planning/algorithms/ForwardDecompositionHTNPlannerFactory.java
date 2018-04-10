package jaicore.planning.algorithms;

import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.task.IHTNPlanningProblem;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearchFactory;
import jaicore.search.algorithms.standard.core.INodeEvaluator;

public class ForwardDecompositionHTNPlannerFactory<V extends Comparable<V>> implements IObservableGraphBasedHTNPlanningAlgorithmFactory<TFDNode,String,V> {

	@Override
	public IObservableGraphBasedHTNPlanningAlgorithm<TFDNode,String> newAlgorithm(IHTNPlanningProblem problem, int numberOfCPUs) {
		return new ForwardDecompositionHTNPlanner(problem, numberOfCPUs);
	}

	@Override
	public IObservableGraphBasedHTNPlanningAlgorithm<TFDNode,String> newAlgorithm(IHTNPlanningProblem problem, IObservableORGraphSearchFactory<TFDNode, String, V> searchFactory, INodeEvaluator<TFDNode, V> nodeEvaluator, int numberOfCPUs) {
		return new ForwardDecompositionHTNPlanner(problem, searchFactory, nodeEvaluator, numberOfCPUs);
	}

}

package jaicore.search.testproblems.knapsack;

import java.util.concurrent.TimeoutException;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.testproblems.knapsack.KnapsackProblem.KnapsackNode;

public class KnapsackToGeneralTraversalTreeReducer implements AlgorithmProblemTransformer<KnapsackProblem, GraphSearchWithSubpathEvaluationsInput<KnapsackNode, String, Double>> {

	@Override
	public GraphSearchWithSubpathEvaluationsInput<KnapsackNode, String, Double> transform(KnapsackProblem problem) {
		return new GraphSearchWithSubpathEvaluationsInput<>(problem.getGraphGenerator(), new INodeEvaluator<KnapsackNode, Double>() {

			@Override
			public Double f(Node<KnapsackNode, ?> node) throws NodeEvaluationException, InterruptedException {
				try {
					return problem.getSolutionEvaluator().evaluateSolution(node.externalPath());
				}
				catch (ObjectEvaluationFailedException | TimeoutException | AlgorithmExecutionCanceledException e) {
					throw new NodeEvaluationException(e, "Could not evaluate node due to an algorithm exception: " + e.getMessage());
				}
			}
		});
	}

}

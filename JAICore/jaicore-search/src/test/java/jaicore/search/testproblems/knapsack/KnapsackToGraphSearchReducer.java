package jaicore.search.testproblems.knapsack;

import java.util.List;
import java.util.Set;

import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.testproblems.knapsack.KnapsackConfiguration;
import jaicore.testproblems.knapsack.KnapsackProblem;

public class KnapsackToGraphSearchReducer implements AlgorithmicProblemReduction<KnapsackProblem, Set<String>, GraphSearchWithSubpathEvaluationsInput<KnapsackConfiguration, String, Double>, SearchGraphPath<KnapsackConfiguration, String>> {

	@Override
	public GraphSearchWithSubpathEvaluationsInput<KnapsackConfiguration, String, Double> encodeProblem(final KnapsackProblem problem) {
		return new GraphSearchWithSubpathEvaluationsInput<>(new KnapsackProblemGraphGenerator(problem), new INodeEvaluator<KnapsackConfiguration, Double>() {

			@Override
			public Double f(final Node<KnapsackConfiguration, ?> node) throws NodeEvaluationException, InterruptedException {
				try {
					List<KnapsackConfiguration> path = node.externalPath();
					return problem.getSolutionEvaluator().evaluate(path.get(path.size() - 1));
				}
				catch (ObjectEvaluationFailedException e) {
					throw new NodeEvaluationException(e, "Could not evaluate node due to an algorithm exception: " + e.getMessage());
				}
			}
		});
	}

	@Override
	public Set<String> decodeSolution(final SearchGraphPath<KnapsackConfiguration, String> solution) {
		return solution.getNodes().get(solution.getNodes().size() - 1).getPackedObjects();
	}

}

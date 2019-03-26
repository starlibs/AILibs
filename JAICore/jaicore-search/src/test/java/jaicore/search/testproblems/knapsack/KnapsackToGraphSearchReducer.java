package jaicore.search.testproblems.knapsack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.testproblems.knapsack.KnapsackConfiguration;
import jaicore.testproblems.knapsack.KnapsackProblem;

public class KnapsackToGraphSearchReducer implements AlgorithmicProblemReduction<KnapsackProblem, Set<String>, GraphSearchWithSubpathEvaluationsInput<KnapsackConfiguration, String, Double>, EvaluatedSearchGraphPath<KnapsackConfiguration, String, Double>> {

	@Override
	public GraphSearchWithSubpathEvaluationsInput<KnapsackConfiguration, String, Double> encodeProblem(final KnapsackProblem problem) {
		return new GraphSearchWithSubpathEvaluationsInput<>(new KnapsackProblemGraphGenerator(problem), new INodeEvaluator<KnapsackConfiguration, Double>() {

			@Override
			public Double f(final Node<KnapsackConfiguration, ?> node) throws NodeEvaluationException, InterruptedException {
				try {
					List<KnapsackConfiguration> path = node.externalPath();
					return problem.getSolutionEvaluator().evaluate(path.get(path.size() - 1));
				}
				catch (ObjectEvaluationFailedException | AlgorithmTimeoutedException e) {
					throw new NodeEvaluationException(e, "Could not evaluate node due to an algorithm exception: " + e.getMessage());
				}
			}
		});
	}

	@Override
	public Set<String> decodeSolution(final EvaluatedSearchGraphPath<KnapsackConfiguration, String, Double> solution) {
		return new HashSet<>(solution.getNodes().get(solution.getNodes().size() - 1).getPackedObjects());
	}

}

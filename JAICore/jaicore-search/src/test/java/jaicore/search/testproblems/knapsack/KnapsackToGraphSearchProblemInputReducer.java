package jaicore.search.testproblems.knapsack;

import java.util.List;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.search.core.interfaces.ISolutionEvaluator;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import jaicore.testproblems.knapsack.KnapsackConfiguration;
import jaicore.testproblems.knapsack.KnapsackProblem;

public class KnapsackToGraphSearchProblemInputReducer implements AlgorithmProblemTransformer<KnapsackProblem, GraphSearchWithPathEvaluationsInput<KnapsackConfiguration, String, Double>> {

	@Override
	public GraphSearchWithPathEvaluationsInput<KnapsackConfiguration, String, Double> transform(final KnapsackProblem problem) {
		return new GraphSearchWithPathEvaluationsInput<>(new KnapsackProblemGraphGenerator(problem), new ISolutionEvaluator<KnapsackConfiguration, Double>() {

			@Override
			public Double evaluateSolution(final List<KnapsackConfiguration> solutionPath) throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, ObjectEvaluationFailedException {
				return problem.getSolutionEvaluator().evaluate(solutionPath.get(solutionPath.size() - 1));
			}

			@Override
			public boolean doesLastActionAffectScoreOfAnySubsequentSolution(final List<KnapsackConfiguration> partialSolutionPath) {
				return true;
			}

			@Override
			public void cancel() {

				/* cannot be canceled */
			}
		});
	}
}

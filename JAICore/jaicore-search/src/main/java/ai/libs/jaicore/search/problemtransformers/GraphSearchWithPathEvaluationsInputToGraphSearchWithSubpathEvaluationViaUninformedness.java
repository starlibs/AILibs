package ai.libs.jaicore.search.problemtransformers;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class GraphSearchWithPathEvaluationsInputToGraphSearchWithSubpathEvaluationViaUninformedness<N, A> implements AlgorithmicProblemReduction<IPathSearchWithPathEvaluationsInput<N, A, Double>, SearchGraphPath<N, A>, GraphSearchWithSubpathEvaluationsInput<N, A, Double>, EvaluatedSearchGraphPath<N, A, Double>> {

	@Override
	public GraphSearchWithSubpathEvaluationsInput<N, A, Double> encodeProblem(final IPathSearchWithPathEvaluationsInput<N, A, Double> problem) {
		IPathEvaluator<N, A, Double> evaluator = new AlternativeNodeEvaluator<>(new IPathEvaluator<N, A, Double>() {

			@Override
			public Double evaluate(final ILabeledPath<N, A> path) throws PathEvaluationException, InterruptedException {
				return problem.getGoalTester().isGoal(path) ? null : 0.0; // goal paths should be evaluated by the ground truth
			}
		}, problem.getPathEvaluator());
		return new GraphSearchWithSubpathEvaluationsInput<>(problem, evaluator);
	}

	@Override
	public SearchGraphPath<N, A> decodeSolution(final EvaluatedSearchGraphPath<N, A, Double> solution) {
		return solution;
	}

}

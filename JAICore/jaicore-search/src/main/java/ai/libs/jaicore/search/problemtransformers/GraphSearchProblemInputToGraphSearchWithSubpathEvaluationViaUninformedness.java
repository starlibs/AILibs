package ai.libs.jaicore.search.problemtransformers;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class GraphSearchProblemInputToGraphSearchWithSubpathEvaluationViaUninformedness<N, A> implements AlgorithmicProblemReduction<IPathSearchInput<N, A>, SearchGraphPath<N, A>, GraphSearchWithSubpathEvaluationsInput<N, A, Double>, EvaluatedSearchGraphPath<N, A, Double>> {

	@Override
	public GraphSearchWithSubpathEvaluationsInput<N, A, Double> encodeProblem(final IPathSearchInput<N, A> problem) {
		IPathEvaluator<N, A, Double> pe;
		if (problem instanceof IPathSearchWithPathEvaluationsInput) {
			IPathSearchWithPathEvaluationsInput<N, A, Double> cProblem = (IPathSearchWithPathEvaluationsInput<N, A, Double>)problem;
			pe = n -> problem.getGoalTester().isGoal(n) ? cProblem.getPathEvaluator().evaluate(n) : 0.0;
		}
		else {
			pe = n -> 0.0;
		}
		return new GraphSearchWithSubpathEvaluationsInput<>(problem, pe);
	}

	@Override
	public SearchGraphPath<N, A> decodeSolution(final EvaluatedSearchGraphPath<N, A, Double> solution) {
		return solution;
	}

}

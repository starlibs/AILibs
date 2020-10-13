package ai.libs.jaicore.search.problemtransformers;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;

import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

/**
 *
 * @author Felix Mohr
 *
 * @param <N> Node Type
 * @param <A> Arc Type
 *
 * This reduction will create a problem in which each inner node is evaluated with 0.0, and each leaf node with the value given by the path evaluator.
 * The given path evaluator is, by contract, just applicable to leaf nodes, so it is just used there, and everywhere else we use just a constant 0.0.
 */
public class GraphSearchProblemInputToGraphSearchWithSubpathEvaluationViaUninformedness<N, A> extends GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<N, A, Double> {

	@Override
	public GraphSearchWithSubpathEvaluationsInput<N, A, Double> encodeProblem(final IPathSearchInput<N, A> problem) {
		if (!(problem instanceof IPathSearchWithPathEvaluationsInput)) {
			throw new IllegalArgumentException("Given problem must be of type " + IPathSearchWithPathEvaluationsInput.class + " but is of " + problem.getClass());
		}
		final IPathSearchWithPathEvaluationsInput<N, A, Double> cProblem = (IPathSearchWithPathEvaluationsInput<N, A, Double>) problem;
		this.setNodeEvaluator(p -> cProblem.getGoalTester().isGoal(p) ? cProblem.getPathEvaluator().evaluate(p) : 0.0);
		return super.encodeProblem(problem);
	}
}

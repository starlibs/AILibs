package ai.libs.jaicore.search.algorithms.standard.bnb;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.search.algorithms.standard.bestfirst.StandardBestFirst;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS;

/**
 * Branch and Bound algorithm. The evaluations given for each node must be optimistic (lower bounds).
 *
 * @author Felix Mohr
 *
 * @param <N>
 * @param <A>
 */
public class BranchAndBound<N, A> extends StandardBestFirst<N, A, Double> {

	public static <N, A> GraphSearchWithSubpathEvaluationsInput<N, A, Double> encodeBoundsIntoProblem(final IPathSearchWithPathEvaluationsInput<N, A, Double> problem, final int numSamplesPerNode) {
		GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<N, A, Double> trans = new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<>(n -> null, n -> false,
				new Random().nextLong(), numSamplesPerNode, 100000, 100000);
		return trans.encodeProblem(problem);
	}

	public BranchAndBound(final IPathSearchWithPathEvaluationsInput<N, A, Double> problem, final IPathEvaluator<N, A, Double> lowerBoundComputer, final int numSamplesPerNode) {
		super(encodeBoundsIntoProblem(problem, numSamplesPerNode), lowerBoundComputer);
	}
}

package jaicore.search.core.interfaces;

import jaicore.search.model.other.SearchGraphPath;

/**
 * Uses Double to be compliant with algorithms that MUST work with double instead of Integer (such as AStar)
 *
 * @author fmohr
 *
 * @param <N>
 */
public class EdgeCountingSolutionEvaluator<N, A> implements ISolutionEvaluator<N, A, Double> {

	@Override
	public Double evaluateSolution(final SearchGraphPath<N, A> solutionPath) {
		return solutionPath.getNodes().size() * 1.0;
	}

	@Override
	public boolean doesLastActionAffectScoreOfAnySubsequentSolution(final SearchGraphPath<N, A> partialSolutionPath) {
		return true;
	}

	@Override
	public void cancel() {
		/* not necessary to explicitly cancel */
	}
}

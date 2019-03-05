package jaicore.search.model.other;

import jaicore.search.core.interfaces.ISolutionEvaluator;

public class AgnosticPathEvaluator<N, A> implements ISolutionEvaluator<N, A, Double> {

	@Override
	public Double evaluateSolution(final SearchGraphPath<N, A> solutionPath) {
		return 0.0;
	}

	@Override
	public boolean doesLastActionAffectScoreOfAnySubsequentSolution(final SearchGraphPath<N, A> partialSolutionPath) {
		return true;
	}

	@Override
	public void cancel() {
		/* not necessary to cancel */
	}
}

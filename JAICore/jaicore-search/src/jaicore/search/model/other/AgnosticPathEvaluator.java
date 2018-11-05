package jaicore.search.model.other;

import java.util.List;

import jaicore.search.core.interfaces.ISolutionEvaluator;

public class AgnosticPathEvaluator<N> implements ISolutionEvaluator<N, Double> {

	@Override
	public Double evaluateSolution(List<N> solutionPath) throws Exception {
		return 0.0;
	}

	@Override
	public boolean doesLastActionAffectScoreOfAnySubsequentSolution(List<N> partialSolutionPath) {
		return true;
	}

	@Override
	public void cancel() {
		/* not necessary to cancel */
	}
}

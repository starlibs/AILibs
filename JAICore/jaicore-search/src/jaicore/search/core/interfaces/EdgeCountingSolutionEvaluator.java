package jaicore.search.core.interfaces;

import java.util.List;

/**
 * Uses Double to be compliant with algorithms that MUST work with double instead of Integer (such as AStar)
 * 
 * @author fmohr
 *
 * @param <N>
 */
public class EdgeCountingSolutionEvaluator<N> implements ISolutionEvaluator<N, Double> {

	@Override
	public Double evaluateSolution(List<N> solutionPath) throws Exception {
		return solutionPath.size() * 1.0;
	}

	@Override
	public boolean doesLastActionAffectScoreOfAnySubsequentSolution(List<N> partialSolutionPath) {
		return true;
	}

	@Override
	public void cancel() {
		/* not necessary to explicitly cancel */
	}
}

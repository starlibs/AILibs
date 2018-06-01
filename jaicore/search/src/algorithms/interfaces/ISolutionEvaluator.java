package jaicore.search.algorithms.interfaces;

import java.util.List;

public interface ISolutionEvaluator<N,V extends Comparable<V>> {
	public V evaluateSolution(List<N> solutionPath) throws Exception;
	
	public boolean doesLastActionAffectScoreOfAnySubsequentSolution(List<N> partialSolutionPath);
}

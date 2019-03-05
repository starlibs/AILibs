package jaicore.search.core.interfaces;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.search.model.other.SearchGraphPath;

public interface ISolutionEvaluator<N, A, V extends Comparable<V>> {
	public V evaluateSolution(SearchGraphPath<N, A> solutionPath) throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, ObjectEvaluationFailedException;

	public boolean doesLastActionAffectScoreOfAnySubsequentSolution(SearchGraphPath<N, A> partialSolutionPath);

	public void cancel();
}

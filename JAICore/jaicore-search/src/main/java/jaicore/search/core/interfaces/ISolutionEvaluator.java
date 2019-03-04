package jaicore.search.core.interfaces;

import java.util.List;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;

public interface ISolutionEvaluator<N, V extends Comparable<V>> {
	public V evaluateSolution(List<N> solutionPath) throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, ObjectEvaluationFailedException;

	public boolean doesLastActionAffectScoreOfAnySubsequentSolution(List<N> partialSolutionPath);

	public void cancel();
}

package jaicore.search.core.interfaces;

import java.util.List;
import java.util.concurrent.TimeoutException;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;

public interface ISolutionEvaluator<N,V extends Comparable<V>> {
	public V evaluateSolution(List<N> solutionPath) throws InterruptedException, TimeoutException, AlgorithmExecutionCanceledException, ObjectEvaluationFailedException;
	
	public boolean doesLastActionAffectScoreOfAnySubsequentSolution(List<N> partialSolutionPath);
	
	public void cancel();
}

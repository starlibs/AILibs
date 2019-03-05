package jaicore.basic.algorithm;

import org.junit.Test;

public abstract class SolutionCandidateIteratorTester<P, I, O> extends GeneralAlgorithmTester<P, I, O> {

	@Test
	public abstract void testThatAnEventForEachPossibleSolutionIsEmittedInSimpleCall() throws Throwable;

	@Test
	public abstract void testThatAnEventForEachPossibleSolutionIsEmittedInParallelizedCall() throws Throwable;

	@Test
	public abstract void testThatIteratorReturnsEachPossibleSolution() throws Throwable;
	
}

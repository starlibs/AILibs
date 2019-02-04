package jaicore.search.algorithms.standard;

import org.junit.Test;

import jaicore.basic.algorithm.GeneralAlgorithmTester;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.probleminputs.GraphSearchInput;

public abstract class ORGraphSearchTester<P, I extends GraphSearchInput<NSrc, ASrc>, O, NSrc, ASrc, NSearch, ASearch> extends GeneralAlgorithmTester<P, I, O> {

	@Test
	public abstract void testThatAnEventForEachPossibleSolutionIsEmittedInSimpleCall() throws Throwable;

	@Test
	public abstract void testThatAnEventForEachPossibleSolutionIsEmittedInParallelizedCall() throws Throwable;

	@Test
	public abstract void testThatIteratorReturnsEachPossibleSolution() throws Throwable;
	
	public abstract IGraphSearchFactory<I, O, NSrc,ASrc, NSearch,ASearch> getFactory();
}

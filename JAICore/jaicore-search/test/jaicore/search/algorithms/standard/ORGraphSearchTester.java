package jaicore.search.algorithms.standard;

import org.junit.Test;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.graph.IGraphAlgorithmListener;
import jaicore.search.core.interfaces.IGraphSearchFactory;

public abstract class ORGraphSearchTester<P, I, O, NSrc, ASrc, V extends Comparable<V>, NSearch, ASearch> {

	@Test
	public abstract void testSequential() throws Throwable;

	@Test
	public abstract void testParallelized() throws Throwable;

	@Test
	public abstract void testIterable() throws Throwable;

	@Test
	public abstract void testInterrupt() throws Throwable;

	@Test
	public abstract void testCancel() throws Throwable;

	public abstract AlgorithmProblemTransformer<P, I> getProblemReducer();

	public abstract IGraphSearchFactory<I, O, NSrc, ASrc, V, NSearch, ASearch, IGraphAlgorithmListener<NSearch, ASearch>> getFactory();
}

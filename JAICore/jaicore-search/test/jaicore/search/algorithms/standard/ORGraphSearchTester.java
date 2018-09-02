package jaicore.search.algorithms.standard;

public abstract class ORGraphSearchTester {

	public abstract void testSequential() throws Throwable;

	public abstract void testParallelized() throws Throwable;

	public abstract void testIterable() throws Throwable;
}

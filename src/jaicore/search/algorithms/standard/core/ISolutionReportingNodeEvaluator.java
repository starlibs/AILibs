package jaicore.search.algorithms.standard.core;

public interface ISolutionReportingNodeEvaluator<T, V extends Comparable<V>> extends INodeEvaluator<T, V> {
	public SolutionEventBus<T> getSolutionEventBus();
}

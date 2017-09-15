package jaicore.search.algorithms.interfaces.solutionannotations;

public interface FTimeComputationAnnotation<T, V extends Comparable<V>> extends SolutionAnnotation<T,V> {
	public int getTimeForFComputation();
}

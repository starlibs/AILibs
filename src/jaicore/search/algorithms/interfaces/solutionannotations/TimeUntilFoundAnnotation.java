package jaicore.search.algorithms.interfaces.solutionannotations;

public interface TimeUntilFoundAnnotation<T, V extends Comparable<V>> extends SolutionAnnotation<T,V> {
	public int getTimeUntilSolutionWasFound();
}

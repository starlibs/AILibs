package jaicore.search.algorithms.interfaces.solutionannotations;

public interface NumberOfNodeAnnotation<T, V extends Comparable<V>> extends SolutionAnnotation<T,V> {
	public int getGenerationNumberOfGoalNode();
}

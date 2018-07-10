package jaicore.search.algorithms.standard.uncertainty;

@FunctionalInterface
public interface ISolutionDistanceMetric <T> {

	public double calculateSolutionDistance(T solution1, T solution2);
	
}

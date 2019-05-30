package jaicore.search.algorithms.standard.uncertainty;

import java.util.List;

@FunctionalInterface
public interface ISolutionDistanceMetric <T> {

	public double calculateSolutionDistance(List<T> solution1, List<T> solution2);
	
}

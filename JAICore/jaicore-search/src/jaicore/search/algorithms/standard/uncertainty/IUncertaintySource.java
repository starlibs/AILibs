package jaicore.search.algorithms.standard.uncertainty;

import java.util.List;

import jaicore.search.structure.core.Node;

@FunctionalInterface
public interface IUncertaintySource <T, V extends Comparable<V>>{

	public double calculateUncertainty (Node<T, ?> n, List<T> solutionPath, List<V> simulationEvaluations);
	
}

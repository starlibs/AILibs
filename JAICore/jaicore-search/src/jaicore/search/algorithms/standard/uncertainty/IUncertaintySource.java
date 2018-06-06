package jaicore.search.algorithms.standard.uncertainty;

import java.util.List;

import jaicore.search.structure.core.Node;

@FunctionalInterface
public interface IUncertaintySource <T>{

	public double calculateUncertainty (Node<T, UncertaintyFMeasure> n, List<T> solutionPath);
	
}

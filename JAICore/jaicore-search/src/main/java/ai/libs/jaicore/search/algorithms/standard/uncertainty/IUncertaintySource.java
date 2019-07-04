package ai.libs.jaicore.search.algorithms.standard.uncertainty;

import java.util.List;

import ai.libs.jaicore.search.model.travesaltree.Node;

@FunctionalInterface
public interface IUncertaintySource <T, V extends Comparable<V>>{

	public double calculateUncertainty (Node<T, V> n, List<List<T>> simulationPaths, List<V> simulationEvaluations);
	
}

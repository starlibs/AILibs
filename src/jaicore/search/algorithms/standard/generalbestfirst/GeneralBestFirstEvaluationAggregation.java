package jaicore.search.algorithms.standard.generalbestfirst;

import java.util.Map;

import jaicore.search.structure.core.Node;

public interface GeneralBestFirstEvaluationAggregation<T> {
	public int aggregate(Map<Node<T,Integer>,Integer> nodes);
}

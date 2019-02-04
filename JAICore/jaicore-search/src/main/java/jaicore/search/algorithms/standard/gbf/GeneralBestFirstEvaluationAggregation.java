package jaicore.search.algorithms.standard.gbf;

import java.util.Map;

import jaicore.search.model.travesaltree.Node;

public interface GeneralBestFirstEvaluationAggregation<T> {
	public int aggregate(Map<Node<T,Integer>,Integer> nodes);
}

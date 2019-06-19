package ai.libs.jaicore.search.algorithms.standard.gbf;

import java.util.Map;

import ai.libs.jaicore.search.model.travesaltree.Node;

public interface GeneralBestFirstEvaluationAggregation<T> {
	public int aggregate(Map<Node<T,Integer>,Integer> nodes);
}

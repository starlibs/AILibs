package util.search.generalbestfirst;

import java.util.Map;

import util.search.core.Node;

public interface GeneralBestFirstEvaluationAggregation<T> {
	public int aggregate(Map<Node<T,Integer>,Integer> nodes);
}

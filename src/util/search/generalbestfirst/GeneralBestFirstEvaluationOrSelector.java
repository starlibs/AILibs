package util.search.generalbestfirst;

import java.util.List;
import java.util.Map;

import util.search.core.Node;

public interface GeneralBestFirstEvaluationOrSelector<T> {
	public List<Node<T,Integer>> getSuccessorRanking(Map<Node<T,Integer>,Integer> nodes);
}

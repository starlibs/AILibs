package jaicore.search.algorithms.standard.generalbestfirst;

import java.util.List;
import java.util.Map;

import jaicore.search.structure.core.Node;

public interface GeneralBestFirstEvaluationOrSelector<T> {
	public List<Node<T,Integer>> getSuccessorRanking(Map<Node<T,Integer>,Integer> nodes);
}

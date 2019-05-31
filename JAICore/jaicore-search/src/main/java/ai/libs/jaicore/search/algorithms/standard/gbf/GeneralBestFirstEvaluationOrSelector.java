package ai.libs.jaicore.search.algorithms.standard.gbf;

import java.util.List;
import java.util.Map;

import ai.libs.jaicore.search.model.travesaltree.Node;

public interface GeneralBestFirstEvaluationOrSelector<T> {
	public List<Node<T,Integer>> getSuccessorRanking(Map<Node<T,Integer>,Integer> nodes);
}

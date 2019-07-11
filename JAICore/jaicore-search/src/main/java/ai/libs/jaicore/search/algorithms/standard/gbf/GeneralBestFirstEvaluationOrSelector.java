package ai.libs.jaicore.search.algorithms.standard.gbf;

import java.util.List;
import java.util.Map;

import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

public interface GeneralBestFirstEvaluationOrSelector<T, A> {
	public List<BackPointerPath<T, A, Integer>> getSuccessorRanking(Map<BackPointerPath<T, A, Integer>, Integer> nodes);
}

package ai.libs.jaicore.search.algorithms.standard.gbf;

import java.util.Map;

import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

public interface GeneralBestFirstEvaluationAggregation<T, A> {
	public int aggregate(Map<BackPointerPath<T, A, Integer>, Integer> nodes);
}

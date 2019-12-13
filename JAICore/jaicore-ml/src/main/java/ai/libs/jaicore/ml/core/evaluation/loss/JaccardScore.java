package ai.libs.jaicore.ml.core.evaluation.loss;

import java.util.Collection;

import ai.libs.jaicore.basic.sets.SetUtil;

public class JaccardScore extends AInstanceMeasure<Collection<Object>> {

	@Override
	public double score(final Collection<Object> expected, final Collection<Object> actual) {
		return ((double) SetUtil.intersection(expected, actual).size()) / SetUtil.union(expected, actual).size();
	}

}

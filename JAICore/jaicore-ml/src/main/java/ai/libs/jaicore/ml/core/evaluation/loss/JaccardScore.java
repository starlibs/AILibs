package ai.libs.jaicore.ml.core.evaluation.loss;

import java.util.Collection;

import ai.libs.jaicore.basic.sets.SetUtil;

public class JaccardScore extends AInstanceMeasure<Collection<Integer>> {

	@Override
	public double score(final Collection<Integer> expected, final Collection<Integer> actual) {
		return ((double) SetUtil.intersection(expected, actual).size()) / SetUtil.union(expected, actual).size();
	}

}

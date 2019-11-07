package ai.libs.jaicore.ml.core.evaluation.loss;

import java.util.Collection;

import ai.libs.jaicore.basic.sets.SetUtil;

public class JaccardScore extends AInstanceMeasure<Collection<String>> {

	@Override
	public double score(final Collection<String> expected, final Collection<String> actual) {
		return ((double) SetUtil.intersection(expected, actual).size()) / SetUtil.union(expected, actual).size();
	}

}

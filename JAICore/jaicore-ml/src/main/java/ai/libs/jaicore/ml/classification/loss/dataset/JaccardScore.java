package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.Collection;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.ml.classification.loss.instance.AInstanceMeasure;

public class JaccardScore extends AInstanceMeasure<Collection<Integer>, Collection<Integer>> {

	@Override
	public double score(final Collection<Integer> expected, final Collection<Integer> actual) {
		return ((double) SetUtil.intersection(expected, actual).size()) / SetUtil.union(expected, actual).size();
	}

}

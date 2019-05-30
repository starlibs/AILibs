package jaicore.ml.core.evaluation.measure.multilabel;

import java.util.Collection;
import java.util.HashSet;

import jaicore.basic.sets.SetUtil;
import jaicore.ml.core.evaluation.measure.ADecomposableDoubleMeasure;

public class JaccardScore extends ADecomposableDoubleMeasure<double[]> {

	@Override
	public Double calculateMeasure(final double[] actual, final double[] expected) {
		Collection<Integer> t = new HashSet<>();
		Collection<Integer> p = new HashSet<>();
		int numLabels = actual.length;
		for (int label = 0; label < numLabels; label++) {
			if (actual[label] == 1) {
				t.add(label);
			}
			if (expected[label] == 1) {
				p.add(label);
			}
		}
		return ((double) SetUtil.intersection(t, p).size()) / SetUtil.union(t, p).size();
	}

}

package jaicore.ml.core.evaluation.measure.multilabel;

import java.util.Collection;
import java.util.HashSet;

import jaicore.basic.sets.SetUtil;
import jaicore.ml.core.evaluation.measure.ADecomposableDoubleMeasure;

public class JaccardMultilabelEvaluator extends ADecomposableDoubleMeasure<double[]> {

	@Override
	public Double calculateMeasure(double[] actual, double[] expected) {
		Collection<Integer> t = new HashSet<>();
		Collection<Integer> p = new HashSet<>();
		int numLabels = actual.length;
		for (int label = 0; label < numLabels; label++) {
			if (actual[label] == 1)
				t.add(label);
			if (expected[label] == 1)
				p.add(label);
		}
		double jaccardScore = SetUtil.intersection(t, p).size() * 1f / SetUtil.union(t, p).size();
		double jaccardError = 1 - jaccardScore;
		return jaccardError;
	}

}

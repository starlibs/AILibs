package jaicore.ml.core.evaluation.measure.multilabel;

import java.util.Collection;
import java.util.HashSet;

import jaicore.basic.sets.SetUtil;
import jaicore.ml.core.evaluation.measure.ADecomposableDoubleMeasure;

public class F1AverageMeasure extends ADecomposableDoubleMeasure<double[]> {
	
	@Override
	public Double calculateMeasure(double[] actual, double[] expected) {
		int numLabels = actual.length;
		Collection<Integer> t = new HashSet<>();
		Collection<Integer> p = new HashSet<>();
		for (int col = 0; col < numLabels; col++) {
			if (actual[col] == 1)
				t.add(col);
			if (expected[col] == 1)
				p.add(col);
		}
		int correctlyClassified = SetUtil.intersection(t, p).size();
		int numberOfPredicted = p.size();
		int numberOfTrue = t.size();
		double precision = numberOfPredicted > 0 ? (correctlyClassified * 1f / numberOfPredicted) : 0;
		double recall = numberOfTrue > 0 ? (correctlyClassified * 1f / numberOfTrue) : 0;
		double f1 = (2f / (1 / precision + 1 / recall));
		return 100 * (1 - f1);
	}
}

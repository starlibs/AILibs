package jaicore.ml.core.evaluation.measure.multilabel;

import jaicore.ml.core.evaluation.measure.ADecomposableDoubleMeasure;

public class HammingLoss extends ADecomposableDoubleMeasure<double[]> {

	@Override
	public Double calculateMeasure(final double[] actual, final double[] expected) {
		int score = 0;
		int numLabels = actual.length;
		for (int label = 0; label < numLabels; label++) {
			if (actual[label] != expected[label]) {
				score++;
			}
		}
		return score * 1.0 / numLabels;
	}

}
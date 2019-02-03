package jaicore.ml.core.evaluation.measure.multilabel;

import jaicore.ml.core.evaluation.measure.ADecomposableDoubleMeasure;

public class HammingLossMultilabelEvaluator extends ADecomposableDoubleMeasure<double[]> {

	@Override
	public Double calculateMeasure(double[] actual, double[] expected) {
		int score = 0;
		int numLabels = actual.length;
		for (int label = 0; label < numLabels; label ++) {
			if (actual[label] != expected[label])
				score ++;
		}
		return score * 1.0 / numLabels;
	}

}

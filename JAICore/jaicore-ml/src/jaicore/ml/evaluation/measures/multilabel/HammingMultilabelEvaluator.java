package jaicore.ml.evaluation.measures.multilabel;

import jaicore.ml.evaluation.measures.ADecomposableDoubleMeasure;

public class HammingMultilabelEvaluator extends ADecomposableDoubleMeasure<double[]> {

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

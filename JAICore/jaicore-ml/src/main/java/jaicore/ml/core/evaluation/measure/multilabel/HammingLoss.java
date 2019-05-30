package jaicore.ml.core.evaluation.measure.multilabel;

import java.util.Arrays;
import java.util.List;

import jaicore.ml.core.evaluation.measure.ADecomposableDoubleMeasure;
import meka.core.Metrics;

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

	@Override
	public Double calculateAvgMeasure(final List<double[]> actual, final List<double[]> expected) {
		double[][] ypred = new double[actual.size()][];
		int[][] ypredint = new int[actual.size()][];
		for (int i = 0; i < actual.size(); i++) {
			ypred[i] = actual.get(i);
			ypredint[i] = Arrays.stream(actual.get(i)).mapToInt(x -> (x >= 0.5) ? 1 : 0).toArray();
		}

		int[][] y = new int[expected.size()][];
		for (int i = 0; i < expected.size(); i++) {
			y[i] = Arrays.stream(expected.get(i)).mapToInt(x -> (x >= 0.5) ? 1 : 0).toArray();
		}

		return Metrics.L_Hamming(y, ypredint);
	}

}
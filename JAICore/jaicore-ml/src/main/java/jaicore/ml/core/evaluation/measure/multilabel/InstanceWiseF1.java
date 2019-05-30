package jaicore.ml.core.evaluation.measure.multilabel;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import meka.core.Metrics;

/**
 * Instance-wise F1 measure for multi-label classifiers.
 *
 * For reference see
 * Wu, Xi-Zhu; Zhou, Zhi-Hua: A Unified View of Multi-Label Performance Measures (ICML / JMLR 2017)
 *
 * @author mwever
 *
 */
public class InstanceWiseF1 extends ADecomposableMultilabelMeasure {

	@Override
	public Double calculateMeasure(final double[] actual, final double[] expected) {
		if (actual.length != expected.length) {
			throw new IllegalArgumentException("Actual and Expected must be of the same length.");
		}

		int predictedAndExpectedSum = Arrays.stream(actual).mapToInt(x -> (int) x).sum() + Arrays.stream(expected).mapToInt(x -> (int) x).sum();
		if (predictedAndExpectedSum == 0) {
			return 0.0;
		}
		int intersection = IntStream.range(0, actual.length).filter(x -> actual[x] == 1.0 && expected[x] == 1.0).map(x -> 1).sum();
		return (2.0 * intersection / predictedAndExpectedSum);
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

		return Metrics.P_FmacroAvgD(y, ypredint);
	}
}

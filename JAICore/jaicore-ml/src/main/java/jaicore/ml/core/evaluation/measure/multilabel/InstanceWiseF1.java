package jaicore.ml.core.evaluation.measure.multilabel;

import java.util.Arrays;
import java.util.stream.IntStream;

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

		int intersection = IntStream.range(0, actual.length).filter(x -> actual[x] == 1.0 && expected[x] == 1.0).map(x -> 1).sum();
		int predictedAndExpectedSum = Arrays.stream(actual).mapToInt(x -> (int) x).sum() + Arrays.stream(expected).mapToInt(x -> (int) x).sum();
		if (predictedAndExpectedSum == 0) {
			return 0.0;
		}
		return (2.0 * intersection / predictedAndExpectedSum);
	}

}

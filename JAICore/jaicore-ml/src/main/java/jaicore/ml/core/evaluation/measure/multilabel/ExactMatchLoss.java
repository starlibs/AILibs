package jaicore.ml.core.evaluation.measure.multilabel;

import java.util.Arrays;

import jaicore.ml.core.evaluation.measure.ADecomposableDoubleMeasure;

public class ExactMatchLoss extends ADecomposableDoubleMeasure<double[]> {
	
	@Override
	public Double calculateMeasure(double[] actual, double[] expected) {
		return Arrays.equals(actual, expected) ? 0.0 : 1.0;
	}
}

package jaicore.ml.evaluation.measures.multilabel;

import java.util.Arrays;

import jaicore.ml.evaluation.measures.ADecomposableDoubleMeasure;

public class ExactMatchLoss extends ADecomposableDoubleMeasure<double[]> {
	
	@Override
	public Double calculateMeasure(double[] actual, double[] expected) {
		return Arrays.equals(actual, expected) ? 0.0 : 1.0;
	}
}

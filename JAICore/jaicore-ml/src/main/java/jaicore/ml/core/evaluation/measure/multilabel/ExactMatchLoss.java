package jaicore.ml.core.evaluation.measure.multilabel;

import java.util.Arrays;

public class ExactMatchLoss extends ADecomposableMultilabelMeasure {

	@Override
	public Double calculateMeasure(final double[] actual, final double[] expected) {
		return Arrays.equals(actual, expected) ? 0.0 : 1.0;
	}
}

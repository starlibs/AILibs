package ai.libs.jaicore.ml.classification.multilabel.loss;

import java.util.Arrays;

import org.api4.java.ai.ml.core.evaluation.loss.IInstanceWiseLossFunction;

public class ExactMatchLoss implements IInstanceWiseLossFunction<double[]> {

	@Override
	public double loss(final double[] actual, final double[] expected) {
		return Arrays.equals(actual, expected) ? 0.0 : 1.0;
	}

}

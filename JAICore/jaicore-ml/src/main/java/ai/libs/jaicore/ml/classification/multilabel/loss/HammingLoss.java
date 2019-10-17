package ai.libs.jaicore.ml.classification.multilabel.loss;

import org.api4.java.ai.ml.core.evaluation.loss.IInstanceWiseLossFunction;

public class HammingLoss implements IInstanceWiseLossFunction<double[]> {

	@Override
	public double loss(final double[] actual, final double[] expected) {
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
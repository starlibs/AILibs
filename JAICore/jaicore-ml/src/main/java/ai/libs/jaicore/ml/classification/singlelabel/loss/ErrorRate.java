package ai.libs.jaicore.ml.classification.singlelabel.loss;

import java.util.List;

import org.api4.java.ai.ml.classification.singlelabel.loss.ISingleLabelClassificationBatchLossFunction;

public class ErrorRate implements ISingleLabelClassificationBatchLossFunction {

	@Override
	public double loss(final List<?> actual, final List<?> expected) {
		if (expected.size() != actual.size()) {
			throw new IllegalArgumentException("Expected and actual list must have the same length.");
		}

		ZeroOneLoss zeroOneLoss = new ZeroOneLoss();
		double sumOfZOLoss = 0.0;
		for (int i = 0; i < expected.size(); i++) {
			sumOfZOLoss += zeroOneLoss.loss(expected.get(i), actual.get(i));
		}
		return sumOfZOLoss / expected.size();
	}

}

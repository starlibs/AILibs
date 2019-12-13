package ai.libs.jaicore.ml.classification.singlelabel.loss;

import java.util.List;

public class ErrorRate extends ASingleLabelClassificationPerformanceMeasure {

	private final ZeroOneLoss zeroOneLoss = new ZeroOneLoss();

	@Override
	public double loss(final List<?> expected, final List<?> actual) {
		this.checkConsistency(expected, actual);
		double sumOfZOLoss = 0.0;
		for (int i = 0; i < expected.size(); i++) {
			sumOfZOLoss += this.zeroOneLoss.loss(expected.get(i), actual.get(i));
		}
		return sumOfZOLoss / expected.size();
	}

}

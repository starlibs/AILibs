package ai.libs.jaicore.ml.classification.singlelabel.loss;

import java.util.List;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;

public class ErrorRate extends ASingleLabelClassificationMeasure {

	private final ZeroOneLoss zeroOneLoss = new ZeroOneLoss();

	@Override
	public double loss(final List<ISingleLabelClassification> expected, final List<ISingleLabelClassification> actual) {
		this.checkConsistency(expected, actual);

		double sumOfZOLoss = 0.0;
		for (int i = 0; i < expected.size(); i++) {
			sumOfZOLoss += this.zeroOneLoss.loss(expected.get(i), actual.get(i));
		}
		return sumOfZOLoss / expected.size();
	}

}

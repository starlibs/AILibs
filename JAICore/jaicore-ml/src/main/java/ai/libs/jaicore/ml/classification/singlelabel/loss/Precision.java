package ai.libs.jaicore.ml.classification.singlelabel.loss;

import java.util.List;

import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassification;

public class Precision extends ASingleLabelClassificationMeasure {

	private final int positiveClass;

	public Precision(final int positiveClass) {
		this.positiveClass = positiveClass;
	}

	@Override
	public double score(final List<ISingleLabelClassification> expected, final List<ISingleLabelClassification> actual) {
		int tp = 0;
		int fp = 0;

		for (int i = 0; i < actual.size(); i++) {
			int actualValue = actual.get(i).getPrediction();
			int expectedValue = expected.get(i).getPrediction();
			if (actualValue == this.positiveClass) {
				if (actualValue == expectedValue) {
					tp++;
				} else {
					fp++;
				}
			}
		}

		if (tp + fp > 0) {
			return (double) tp / (tp + fp);
		} else {
			return 0;
		}
	}
}

package ai.libs.jaicore.ml.classification.singlelabel.loss;

import java.util.List;

public class Precision extends ASingleLabelClassificationMeasure {

	private final int positiveClass;

	public Precision(final int positiveClass) {
		this.positiveClass = positiveClass;
	}

	@Override
	public double score(final List<Object> expected, final List<Object> actual) {
		int tp = 0;
		int fp = 0;

		for (int i = 0; i < actual.size(); i++) {
			int actualValue = (int)actual.get(i);
			int expectedValue = (int)expected.get(i);
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

package ai.libs.jaicore.ml.classification.singlelabel.loss;

import java.util.List;

import org.api4.java.ai.ml.classification.execution.IClassificationPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.loss.ILossFunction;

public class PrecisionAsLoss implements ILossFunction {

	private final int positiveClass;
	private final double threshold;

	public PrecisionAsLoss(final int positiveClass, final double threshold) {
		this.positiveClass = positiveClass;
		this.threshold = threshold;
	}

	public PrecisionAsLoss(final int positiveClass) {
		this(positiveClass, 0.5);
	}

	@Override
	public double loss(final List<?> expected, final List<?> actual) {
		int tp = 0;
		int fp = 0;

		for (int i = 0; i < actual.size(); i++) {
			int actualValue = ((int)actual.get(i) > this.threshold) ? 1 : 0;
			int expectedValue = (int) expected.get(i);

			if (actualValue == this.positiveClass) {
				if (actualValue == expectedValue) {
					tp++;
				} else {
					fp++;
				}
			}
		}

		double precision;
		if (tp + fp > 0) {
			precision = (double) tp / (tp + fp);
		} else {
			precision = 0;
		}

		return 1 - precision;
	}

	@Override
	public double loss(final IClassificationPredictionAndGroundTruthTable pairTable) {
		return this.loss(pairTable.getGroundTruthAsList(), pairTable.getPredictionsAsList());
	}

}

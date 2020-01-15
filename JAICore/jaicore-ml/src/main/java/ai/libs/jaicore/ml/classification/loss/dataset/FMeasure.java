package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.List;

public class FMeasure extends AHomogeneousPredictionPerformanceMeasure<Object> {

	private final double beta;
	private final Precision precision;
	private final Recall recall;

	public FMeasure(final double beta, final Object positiveClass) {
		this.beta = beta;
		this.precision = new Precision(positiveClass);
		this.recall = new Recall(positiveClass);
	}

	@Override
	public double score(final List<?> expected, final List<?> actual) {
		if (expected.size() != actual.size()) {
			throw new IllegalArgumentException("Expected and actual must be of the same length.");
		}

		double vPrecision = this.precision.score(expected, actual);
		double vRecall = this.recall.score(expected, actual);
		double denominator = ((Math.pow(this.beta, 2) * vPrecision) + vRecall);

		return denominator == 0.0 ? 0 : (1 + Math.pow(this.beta, 2)) * (vPrecision * vRecall) / denominator;
	}

}

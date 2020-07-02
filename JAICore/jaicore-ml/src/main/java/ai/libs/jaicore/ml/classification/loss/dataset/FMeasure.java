package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.List;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;

public class FMeasure extends ASingleLabelPredictionPerformanceMeasure {

	private final double beta;
	private final Precision precision;
	private final Recall recall;

	public FMeasure(final double beta, final int positiveClass) {
		this.beta = beta;
		this.precision = new Precision(positiveClass);
		this.recall = new Recall(positiveClass);
	}

	@Override
	public double score(final List<? extends Integer> expected, final List<? extends ISingleLabelClassification> predicted) {
		if (expected.size() != predicted.size()) {
			throw new IllegalArgumentException("Expected and actual must be of the same length.");
		}

		double vPrecision = this.precision.score(expected, predicted);
		double vRecall = this.recall.score(expected, predicted);
		double denominator = ((Math.pow(this.beta, 2) * vPrecision) + vRecall);

		return denominator == 0.0 ? 0 : (1 + Math.pow(this.beta, 2)) * (vPrecision * vRecall) / denominator;
	}

}

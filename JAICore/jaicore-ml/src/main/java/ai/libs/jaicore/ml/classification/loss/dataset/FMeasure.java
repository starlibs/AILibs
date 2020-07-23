package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.List;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;

import ai.libs.jaicore.basic.metric.ConfusionMetrics;

public class FMeasure extends ASingleLabelClassificationPerformanceMeasure {

	private final double beta;
	private final TruePositives tp;
	private final FalsePositives fp;
	private final FalseNegatives fn;

	public FMeasure(final double beta, final int positiveClass) {
		this.beta = beta;
		this.tp = new TruePositives(positiveClass);
		this.fp = new FalsePositives(positiveClass);
		this.fn = new FalseNegatives(positiveClass);
	}

	@Override
	public double score(final List<? extends Integer> expected, final List<? extends ISingleLabelClassification> predicted) {
		if (expected.size() != predicted.size()) {
			throw new IllegalArgumentException("Expected and actual must be of the same length.");
		}
		return ConfusionMetrics.getFMeasure(this.beta, (int) this.tp.score(expected, predicted), (int) this.fp.score(expected, predicted), (int) this.fn.score(expected, predicted));
	}

}

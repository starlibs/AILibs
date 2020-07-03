package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.List;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;

import ai.libs.jaicore.basic.metric.ConfusionMetrics;

public class Precision extends ASingleLabelPredictionPerformanceMeasure {

	private final TruePositives tp;
	private final FalsePositives fp;

	public Precision(final int positiveClass) {
		this.tp = new TruePositives(positiveClass);
		this.fp = new FalsePositives(positiveClass);
	}

	@Override
	public double score(final List<? extends Integer> expected, final List<? extends ISingleLabelClassification> predicted) {
		return ConfusionMetrics.getPrecision((int) this.tp.score(expected, predicted), (int) this.fp.score(expected, predicted));
	}

}

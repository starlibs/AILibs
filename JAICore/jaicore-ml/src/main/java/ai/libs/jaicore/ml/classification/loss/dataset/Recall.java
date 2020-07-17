package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.List;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;

import ai.libs.jaicore.basic.metric.ConfusionMetrics;

public class Recall extends ASingleLabelClassificationPerformanceMeasure {

	private final TruePositives tp;
	private final FalseNegatives fn;

	public Recall(final int positiveClass) {
		this.tp = new TruePositives(positiveClass);
		this.fn = new FalseNegatives(positiveClass);
	}

	@Override
	public double score(final List<? extends Integer> expected, final List<? extends ISingleLabelClassification> predicted) {
		return ConfusionMetrics.getRecall((int) this.tp.score(expected, predicted), (int) this.fn.score(expected, predicted));
	}

}

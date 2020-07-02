package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.List;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;

public class Recall extends ASingleLabelPredictionPerformanceMeasure {

	private final TruePositives tp;
	private final FalseNegatives fn;

	public Recall(final int positiveClass) {
		this.tp = new TruePositives(positiveClass);
		this.fn = new FalseNegatives(positiveClass);
	}

	@Override
	public double score(final List<? extends Integer> expected, final List<? extends ISingleLabelClassification> predicted) {
		double truePositives = this.tp.score(expected, predicted);
		double denominator = (truePositives + this.fn.score(expected, predicted));
		return denominator == 0.0 ? 0 : truePositives / denominator;
	}

}

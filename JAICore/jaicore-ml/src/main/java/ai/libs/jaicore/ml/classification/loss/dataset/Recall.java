package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.List;

public class Recall extends AHomogeneousPredictionPerformanceMeasure<Object>  {

	private final TruePositives tp;
	private final FalseNegatives fn;

	public Recall(final Object positiveClass) {
		this.tp = new TruePositives(positiveClass);
		this.fn = new FalseNegatives(positiveClass);
	}

	@Override
	public double score(final List<? extends Object> expected, final List<? extends Object> predicted) {
		double truePositives = this.tp.score(expected, predicted);
		double denominator = (truePositives + this.fn.score(expected, predicted));
		return denominator == 0.0 ? 0 : truePositives / denominator;
	}

}

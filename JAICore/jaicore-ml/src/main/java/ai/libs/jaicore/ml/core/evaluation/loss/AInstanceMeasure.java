package ai.libs.jaicore.ml.core.evaluation.loss;

import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicInstancePredictionPerformanceMeasure;

public class AInstanceMeasure<O> implements IDeterministicInstancePredictionPerformanceMeasure<O> {

	@Override
	public double loss(final O expected, final O actual) {
		return 1 - this.score(expected, actual);
	}

	@Override
	public double score(final O expected, final O actual) {
		return 1 - this.loss(expected, actual);
	}

}

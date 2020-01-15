package ai.libs.jaicore.ml.classification.loss.instance;

import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicInstancePredictionPerformanceMeasure;

public class AInstanceMeasure<O, P> implements IDeterministicInstancePredictionPerformanceMeasure<O, P> {

	@Override
	public double loss(final P expected, final O actual) {
		return 1 - this.score(expected, actual);
	}

	@Override
	public double score(final P expected, final O actual) {
		return 1 - this.loss(expected, actual);
	}

}

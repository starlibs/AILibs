package ai.libs.jaicore.ml.classification.loss.instance;

import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicInstancePredictionPerformanceMeasure;

/**
 *
 * @author mwever
 */
public class ZeroOneLoss implements IDeterministicInstancePredictionPerformanceMeasure<Object, Object> {

	@Override
	public double loss(final Object expected, final Object actual) {
		return expected.equals(actual) ? 0.0 : 1.0;
	}

	@Override
	public double score(final Object expected, final Object actual) {
		return 1 - this.loss(expected, actual);
	}

}

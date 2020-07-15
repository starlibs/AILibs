package ai.libs.jaicore.ml.classification.loss.instance;

import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicInstancePredictionPerformanceMeasure;

/**
 * Abstract class for instance-based measures.
 *
 * @author mwever
 *
 * @param <E> The type of the expected value.
 * @param <A> The type of the actual/predicted.
 */
public class AInstanceMeasure<E, A> implements IDeterministicInstancePredictionPerformanceMeasure<A, E> {

	@Override
	public double loss(final E expected, final A predicted) {
		return 1 - this.score(expected, predicted);
	}

	@Override
	public double score(final E expected, final A predicted) {
		return 1 - this.loss(expected, predicted);
	}

}

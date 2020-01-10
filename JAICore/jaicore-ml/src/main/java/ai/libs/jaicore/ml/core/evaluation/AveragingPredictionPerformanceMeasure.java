package ai.libs.jaicore.ml.core.evaluation;

import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;

import ai.libs.jaicore.basic.aggregate.reals.Mean;

public class AveragingPredictionPerformanceMeasure<E, A> extends AggregatingPredictionPerformanceMeasure<E, A> {

	private static final Mean MEAN = new Mean();

	public AveragingPredictionPerformanceMeasure(final IDeterministicPredictionPerformanceMeasure<E, A> baseMeasure) {
		super(MEAN, baseMeasure);
	}
}

package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicInstancePredictionPerformanceMeasure;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;

public abstract class APredictionPerformanceMeasure<E, P> implements IDeterministicPredictionPerformanceMeasure<E, P> {

	public APredictionPerformanceMeasure() {
		super();
	}

	protected void checkConsistency(final List<? extends E> expected, final List<? extends P> predicted) {
		if (expected.size() != predicted.size()) {
			throw new IllegalArgumentException("The expected and predicted classification lists must be of the same length.");
		}
	}

	@Override
	public double loss(final IPredictionAndGroundTruthTable<? extends E, ? extends P> pairTable) {
		return this.loss(pairTable.getGroundTruthAsList(), pairTable.getPredictionsAsList());
	}

	/**
	 * If this performance measure is originally a score function its score is transformed into a loss by multiplying the score with -1. (loss=-score).
	 */
	@Override
	public double loss(final List<? extends E> expected, final List<? extends P> predicted) {
		return -this.score(expected, predicted);
	}

	/**
	 * If this performance measure is originally a loss function its loss is transformed into a score by multiplying the loss with -1. (score=-loss).
	 */
	@Override
	public double score(final List<? extends E> expected, final List<? extends P> predicted) {
		return -this.loss(expected, predicted);
	}

	@Override
	public double score(final IPredictionAndGroundTruthTable<? extends E, ? extends P> pairTable) {
		return this.score(pairTable.getGroundTruthAsList(), pairTable.getPredictionsAsList());
	}

	@Deprecated // Needs to be adapted with the new api4 release
	protected double averageInstanceWiseLoss(final List<? extends E> expected, final List<? extends P> predicted, final IDeterministicInstancePredictionPerformanceMeasure<P, E> subMeasure) {
		OptionalDouble res = IntStream.range(0, expected.size()).mapToDouble(x -> subMeasure.loss(expected.get(x), predicted.get(x))).average();
		if (res.isPresent()) {
			return res.getAsDouble();
		} else {
			throw new IllegalStateException("The submeasure could not be aggregated.");
		}
	}

	@Deprecated // Needs to be adapted with the new api4 release
	protected double averageInstanceWiseScore(final List<? extends E> expected, final List<? extends P> predicted, final IDeterministicInstancePredictionPerformanceMeasure<P, E> subMeasure) {
		OptionalDouble res = IntStream.range(0, expected.size()).mapToDouble(x -> subMeasure.score(expected.get(x), predicted.get(x))).average();
		if (res.isPresent()) {
			return res.getAsDouble();
		} else {
			throw new IllegalStateException("The submeasure could not be aggregated.");
		}
	}
}

package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicInstancePredictionPerformanceMeasure;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;

public abstract class APredictionPerformanceMeasure<E, A> implements IDeterministicPredictionPerformanceMeasure<E, A> {

	protected void checkConsistency(final List<? extends E> expected, final List<? extends A> actual) {
		if (expected.size() != actual.size()) {
			throw new IllegalArgumentException("The expected and predicted classification lists must be of the same length.");
		}
	}

	@Override
	public double loss(final IPredictionAndGroundTruthTable<? extends E, ? extends A> pairTable) {
		return this.loss(pairTable.getGroundTruthAsList(), pairTable.getPredictionsAsList());
	}

	@Override
	public double loss(final List<? extends E> expected, final List<? extends A> actual) {
		return 1 - this.score(expected, actual);
	}

	@Override
	public double score(final List<? extends E> expected, final List<? extends A> actual) {
		return 1 - this.loss(expected, actual);
	}

	@Override
	public double score(final IPredictionAndGroundTruthTable<? extends E, ? extends A> pairTable) {
		return this.score(pairTable.getGroundTruthAsList(), pairTable.getPredictionsAsList());
	}

	protected double averageInstanceWiseLoss(final List<E> expected, final List<A> actual, final IDeterministicInstancePredictionPerformanceMeasure<A, E> subMeasure) {
		OptionalDouble res = IntStream.range(0, expected.size()).mapToDouble(x -> subMeasure.loss(expected.get(x), actual.get(x))).average();
		if (res.isPresent()) {
			return res.getAsDouble();
		} else {
			throw new IllegalStateException("The submeasure could not be aggregated.");
		}
	}

	protected double averageInstanceWiseScore(final List<E> expected, final List<A> actual, final IDeterministicInstancePredictionPerformanceMeasure<A, E> subMeasure) {
		OptionalDouble res = IntStream.range(0, expected.size()).mapToDouble(x -> subMeasure.score(expected.get(x), actual.get(x))).average();
		if (res.isPresent()) {
			return res.getAsDouble();
		} else {
			throw new IllegalStateException("The submeasure could not be aggregated.");
		}
	}
}

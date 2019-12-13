package ai.libs.jaicore.ml.core.evaluation.loss;

import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicInstancePredictionPerformanceMeasure;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;

public abstract class APredictionPerformanceMeasure<O> implements IDeterministicPredictionPerformanceMeasure<O> {

	protected void checkConsistency(final List<O> expected, final List<O> actual) {
		if (expected.size() != actual.size()) {
			throw new IllegalArgumentException("The expected and predicted classification lists must be of the same length.");
		}
	}

	@Override
	public double loss(final IPredictionAndGroundTruthTable<O> pairTable) {
		return this.loss(pairTable.getPredictionsAsList(), pairTable.getGroundTruthAsList());
	}

	@Override
	public double loss(final List<O> actual, final List<O> expected) {
		return 1 - this.score(expected, actual);
	}

	@Override
	public double score(final List<O> expected, final List<O> actual) {
		return 1 - this.loss(actual, expected);
	}

	@Override
	public double score(final IPredictionAndGroundTruthTable<O> pairTable) {
		return this.score(pairTable.getGroundTruthAsList(), pairTable.getPredictionsAsList());
	}

	protected double averageInstanceWiseLoss(final List<O> expected, final List<O> actual, final IDeterministicInstancePredictionPerformanceMeasure<O> subMeasure) {
		OptionalDouble res = IntStream.range(0, expected.size()).mapToDouble(x -> subMeasure.loss(expected.get(x), actual.get(x))).average();
		if (res.isPresent()) {
			return res.getAsDouble();
		} else {
			throw new IllegalStateException("The submeasure could not be aggregated.");
		}
	}

	protected double averageInstanceWiseScore(final List<O> expected, final List<O> actual, final IDeterministicInstancePredictionPerformanceMeasure<O> subMeasure) {
		OptionalDouble res = IntStream.range(0, expected.size()).mapToDouble(x -> subMeasure.score(expected.get(x), actual.get(x))).average();
		if (res.isPresent()) {
			return res.getAsDouble();
		} else {
			throw new IllegalStateException("The submeasure could not be aggregated.");
		}
	}
}

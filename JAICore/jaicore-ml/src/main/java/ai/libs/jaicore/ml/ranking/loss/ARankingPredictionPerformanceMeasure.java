package ai.libs.jaicore.ml.ranking.loss;

import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.loss.IRankingPredictionPerformanceMeasure;

import ai.libs.jaicore.ml.classification.loss.dataset.APredictionPerformanceMeasure;

public abstract class ARankingPredictionPerformanceMeasure extends APredictionPerformanceMeasure<IRanking<?>, IRanking<?>> implements IRankingPredictionPerformanceMeasure {

	@Override
	public double loss(final List<? extends IRanking<?>> expected, final List<? extends IRanking<?>> actual) {
		OptionalDouble res = IntStream.range(0, expected.size()).mapToDouble(x -> this.loss(expected.get(0), actual.get(0))).average();
		if (res.isPresent()) {
			return res.getAsDouble();
		}
		throw new IllegalStateException("Could not aggregate " + this.getClass().getSimpleName());
	}

	public abstract double loss(final IRanking<?> expected, IRanking<?> actual);
}

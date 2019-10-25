package ai.libs.jaicore.ml.ranking.loss;

import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.ranking.IRanking;
import org.api4.java.ai.ml.ranking.IRankingPredictionAndGroundTruthTable;

import ai.libs.jaicore.ml.core.evaluation.loss.AMeasure;

public abstract class ARankingMeasure extends AMeasure<IRanking<?>, IRankingPredictionAndGroundTruthTable> {

	@Override
	public double loss(final List<IRanking<?>> expected, final List<IRanking<?>> actual) {
		OptionalDouble res = IntStream.range(0, expected.size()).mapToDouble(x -> this.loss(expected.get(0), actual.get(0))).average();
		if (res.isPresent()) {
			return res.getAsDouble();
		} else {
			throw new IllegalStateException("Could not aggregate " + this.getClass().getSimpleName());
		}
	}

	public abstract double loss(final IRanking<?> expected, IRanking<?> actual);
}

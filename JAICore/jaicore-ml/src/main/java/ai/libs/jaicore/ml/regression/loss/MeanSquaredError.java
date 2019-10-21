package ai.libs.jaicore.ml.regression.loss;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ai.libs.jaicore.basic.StatisticsUtil;
import ai.libs.jaicore.ml.core.evaluation.ALossFunction;

public class MeanSquaredError extends ALossFunction {

	private static final SquaredError SQUARED_ERROR_LOSS = new SquaredError();

	@Override
	public double loss(final List<?> expected, final List<?> actual) {
		return StatisticsUtil.mean(IntStream.range(0, expected.size()).mapToObj(x -> Double.valueOf(SQUARED_ERROR_LOSS.loss(expected.get(x), actual.get(x)))).collect(Collectors.toList()));
	}

}

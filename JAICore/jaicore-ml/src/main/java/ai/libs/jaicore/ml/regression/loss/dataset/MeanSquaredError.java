package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ai.libs.jaicore.basic.StatisticsUtil;
import ai.libs.jaicore.ml.regression.loss.instance.SquaredError;

public class MeanSquaredError extends ARegressionMeasure {

	private static final SquaredError SQUARED_ERROR_LOSS = new SquaredError();

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends Double> actual) {
		return StatisticsUtil.mean(IntStream.range(0, expected.size()).mapToObj(x -> Double.valueOf(SQUARED_ERROR_LOSS.loss(expected.get(x), actual.get(x)))).collect(Collectors.toList()));
	}

}

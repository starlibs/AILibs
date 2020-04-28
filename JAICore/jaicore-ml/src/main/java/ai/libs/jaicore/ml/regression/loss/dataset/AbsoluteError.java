package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ai.libs.jaicore.basic.StatisticsUtil;

public class AbsoluteError extends ARegressionMeasure {

	private static final ai.libs.jaicore.ml.regression.loss.instance.AbsoluteError ABSOLUTE_ERROR_LOSS = new ai.libs.jaicore.ml.regression.loss.instance.AbsoluteError();

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends Double> actual) {
		return StatisticsUtil.sum(IntStream.range(0, expected.size()).mapToObj(x -> Double.valueOf(ABSOLUTE_ERROR_LOSS.loss(expected.get(x), actual.get(x)))).collect(Collectors.toList()));
	}

}
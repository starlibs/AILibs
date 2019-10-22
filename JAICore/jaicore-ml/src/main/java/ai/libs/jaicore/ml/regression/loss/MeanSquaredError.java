package ai.libs.jaicore.ml.regression.loss;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.evaluation.loss.ILossFunction;

import ai.libs.jaicore.basic.StatisticsUtil;

public class MeanSquaredError implements ILossFunction<Double> {

	private static final SquaredError SQUARED_ERROR_LOSS = new SquaredError();

	@Override
	public double loss(final List<Double> actual, final List<Double> expected) {
		return StatisticsUtil.mean(IntStream.range(0, expected.size()).mapToObj(x -> Double.valueOf(SQUARED_ERROR_LOSS.loss(expected.get(x), actual.get(x)))).collect(Collectors.toList()));
	}

}

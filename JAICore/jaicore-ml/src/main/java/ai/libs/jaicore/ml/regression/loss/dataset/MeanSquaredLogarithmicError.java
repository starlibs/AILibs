package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;

import ai.libs.jaicore.basic.StatisticsUtil;
import ai.libs.jaicore.ml.regression.loss.instance.SquaredLogarithmicError;

public class MeanSquaredLogarithmicError extends ARegressionMeasure {

	private static final SquaredLogarithmicError SQUARED_LOGARITHMIC_LOSS = new SquaredLogarithmicError();

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends IRegressionPrediction> predicted) {
		return StatisticsUtil.mean(IntStream.range(0, expected.size()).mapToObj(x -> Double.valueOf(SQUARED_LOGARITHMIC_LOSS.loss(expected.get(x), predicted.get(x).getPrediction()))).collect(Collectors.toList()));
	}

}
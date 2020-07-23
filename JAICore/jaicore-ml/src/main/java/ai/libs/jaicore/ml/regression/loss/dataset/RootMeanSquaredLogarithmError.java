package ai.libs.jaicore.ml.regression.loss.dataset;

import java.util.List;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;

public class RootMeanSquaredLogarithmError extends ARegressionMeasure {

	public RootMeanSquaredLogarithmError() {
		super();
	}

	@Override
	public double score(final List<? extends Double> expected, final List<? extends IRegressionPrediction> predicted) {
		this.checkConsistency(expected, predicted);
		return Math.sqrt(IntStream.range(0, expected.size()).mapToDouble(x -> predicted.get(x).getPrediction() - expected.get(x)) // error
				.map(Math::log) // log
				.map(x -> Math.pow(x, 2)) // squared
				.average().getAsDouble() // mean
		); // root
	}

}

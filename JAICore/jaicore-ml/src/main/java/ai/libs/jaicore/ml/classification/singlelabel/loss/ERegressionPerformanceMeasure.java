package ai.libs.jaicore.ml.classification.singlelabel.loss;

import java.util.List;

import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicHomogeneousPredictionPerformanceMeasure;

import ai.libs.jaicore.ml.regression.loss.MeanSquaredError;
import ai.libs.jaicore.ml.regression.loss.RootMeanSquaredError;

public enum ERegressionPerformanceMeasure implements IDeterministicHomogeneousPredictionPerformanceMeasure<Double> {
	MEAN_SQUARED_ERROR(new MeanSquaredError()), ROOT_MEAN_SQUARED_ERROR(new RootMeanSquaredError());

	private final IDeterministicHomogeneousPredictionPerformanceMeasure<Double> measure;

	private ERegressionPerformanceMeasure(final IDeterministicHomogeneousPredictionPerformanceMeasure<Double> measure) {
		this.measure = measure;
	}

	@Override
	public double loss(final List<Double> actual, final List<Double> expected) {
		return this.measure.loss(actual, expected);
	}

	@Override
	public double loss(final IPredictionAndGroundTruthTable<Double, Double> pairTable) {
		return this.measure.loss(pairTable);
	}

	@Override
	public double score(final List<Double> expected, final List<Double> actual) {
		return this.measure.score(actual, expected);
	}

	@Override
	public double score(final IPredictionAndGroundTruthTable<Double, Double> pairTable) {
		return this.measure.score(pairTable);
	}
}

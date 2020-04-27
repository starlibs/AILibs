package ai.libs.jaicore.ml.regression.loss;

import java.util.List;

import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicHomogeneousPredictionPerformanceMeasure;

import ai.libs.jaicore.ml.regression.loss.dataset.AbsoluteError;
import ai.libs.jaicore.ml.regression.loss.dataset.AsymmetricLoss;
import ai.libs.jaicore.ml.regression.loss.dataset.LinearMeanSquaredError;
import ai.libs.jaicore.ml.regression.loss.dataset.MeanSquaredLogarithmicMeanSquaredError;
import ai.libs.jaicore.ml.regression.loss.dataset.QuadraticQuadraticError;
import ai.libs.jaicore.ml.regression.loss.dataset.SquaredError;
import ai.libs.jaicore.ml.regression.loss.dataset.WeightedAbsoluteError;
import ai.libs.jaicore.ml.regression.loss.dataset.WeightedAsymmetricAbsoluteError;

public enum ERulPerformanceMeasure implements IDeterministicHomogeneousPredictionPerformanceMeasure<Double> {
	ASYMMETRIC_LOSS(new AsymmetricLoss()), ABSOLUTE_ERROR(new AbsoluteError()), SQUARED_ERROR(new SquaredError()), WEIGHTED_ABSOLUTE_ERROR(new WeightedAbsoluteError()), WEIGHTED_ASYMMETRIC_ABSOLUTE_ERROR(
			new WeightedAsymmetricAbsoluteError()), LINEAR_MEAN_SQUARED_ERROR(
					new LinearMeanSquaredError()), MEAN_SQUARED_LOGARITHMIC_MEAN_SQUARED_ERROR(new MeanSquaredLogarithmicMeanSquaredError()), QUADRATIC_QUADRATIC_ERROR(new QuadraticQuadraticError());

	private final IDeterministicHomogeneousPredictionPerformanceMeasure<Double> measure;

	private ERulPerformanceMeasure(final IDeterministicHomogeneousPredictionPerformanceMeasure<Double> measure) {
		this.measure = measure;
	}

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends Double> actual) {
		return this.measure.loss(actual, expected);
	}

	@Override
	public double loss(final IPredictionAndGroundTruthTable<? extends Double, ? extends Double> pairTable) {
		return this.measure.loss(pairTable);
	}

	@Override
	public double score(final List<? extends Double> expected, final List<? extends Double> actual) {
		return this.measure.score(actual, expected);
	}

	@Override
	public double score(final IPredictionAndGroundTruthTable<? extends Double, ? extends Double> pairTable) {
		return this.measure.score(pairTable);
	}

}

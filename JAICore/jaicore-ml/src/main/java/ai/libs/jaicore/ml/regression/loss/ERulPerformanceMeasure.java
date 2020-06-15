package ai.libs.jaicore.ml.regression.loss;

import java.util.List;

import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicHomogeneousPredictionPerformanceMeasure;

import ai.libs.jaicore.ml.regression.loss.dataset.AsymmetricLoss;
import ai.libs.jaicore.ml.regression.loss.dataset.AsymmetricLoss2;
import ai.libs.jaicore.ml.regression.loss.dataset.LinearMeanSquaredError;
import ai.libs.jaicore.ml.regression.loss.dataset.MeanAbsoluteError;
import ai.libs.jaicore.ml.regression.loss.dataset.MeanAbsolutePercentageError;
import ai.libs.jaicore.ml.regression.loss.dataset.MeanAsymmetricLoss2;
import ai.libs.jaicore.ml.regression.loss.dataset.MeanPercentageError;
import ai.libs.jaicore.ml.regression.loss.dataset.MeanSquaredError;
import ai.libs.jaicore.ml.regression.loss.dataset.MeanSquaredLogarithmicMeanSquaredError;
import ai.libs.jaicore.ml.regression.loss.dataset.MeanSquaredPercentageError;
import ai.libs.jaicore.ml.regression.loss.dataset.QuadraticQuadraticError;
import ai.libs.jaicore.ml.regression.loss.dataset.RootMeanSquaredError;
import ai.libs.jaicore.ml.regression.loss.dataset.WeightedAbsoluteError;
import ai.libs.jaicore.ml.regression.loss.dataset.WeightedAsymmetricAbsoluteError;

public enum ERulPerformanceMeasure implements IDeterministicHomogeneousPredictionPerformanceMeasure<Double> {
	ASYMMETRIC_LOSS(new AsymmetricLoss()), ASYMMETRIC_LOSS2(new AsymmetricLoss2()), MEAN_ASYMMETRIC_LOSS2(new MeanAsymmetricLoss2()), MEAN_PERCENTAGE_ERROR(new MeanPercentageError()), MEAN_ABSOLUTE_PERCENTAGE_ERROR(
			new MeanAbsolutePercentageError()), MEAN_SQUARED_PERCENTAGE_ERROR(new MeanSquaredPercentageError()), MEAN_ABSOLUTE_ERROR(new MeanAbsoluteError()), ROOT_MEAN_SQUARED_ERROR(new RootMeanSquaredError()), MEAN_SQUARED_ERROR(
					new MeanSquaredError()), WEIGHTED_ABSOLUTE_ERROR(new WeightedAbsoluteError()), WEIGHTED_ASYMMETRIC_ABSOLUTE_ERROR(new WeightedAsymmetricAbsoluteError()), LINEAR_MEAN_SQUARED_ERROR(
							new LinearMeanSquaredError()), MEAN_SQUARED_LOGARITHMIC_MEAN_SQUARED_ERROR(new MeanSquaredLogarithmicMeanSquaredError()), QUADRATIC_QUADRATIC_ERROR(new QuadraticQuadraticError());

	private final IDeterministicHomogeneousPredictionPerformanceMeasure<Double> measure;

	private ERulPerformanceMeasure(final IDeterministicHomogeneousPredictionPerformanceMeasure<Double> measure) {
		this.measure = measure;
	}

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends Double> actual) {
		return this.measure.loss(expected, actual);
	}

	@Override
	public double loss(final IPredictionAndGroundTruthTable<? extends Double, ? extends Double> pairTable) {
		return this.measure.loss(pairTable);
	}

	@Override
	public double score(final List<? extends Double> expected, final List<? extends Double> actual) {
		return this.measure.score(expected, actual);
	}

	@Override
	public double score(final IPredictionAndGroundTruthTable<? extends Double, ? extends Double> pairTable) {
		return this.measure.score(pairTable);
	}

}

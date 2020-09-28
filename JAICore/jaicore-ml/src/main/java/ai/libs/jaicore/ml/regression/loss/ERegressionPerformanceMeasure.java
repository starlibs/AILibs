package ai.libs.jaicore.ml.regression.loss;

import java.util.List;

import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;

import ai.libs.jaicore.ml.regression.loss.dataset.MeanAbsoluteError;
import ai.libs.jaicore.ml.regression.loss.dataset.MeanAbsolutePercentageError;
import ai.libs.jaicore.ml.regression.loss.dataset.MeanSquaredError;
import ai.libs.jaicore.ml.regression.loss.dataset.R2;
import ai.libs.jaicore.ml.regression.loss.dataset.RootMeanSquaredError;
import ai.libs.jaicore.ml.regression.loss.dataset.RootMeanSquaredLogarithmError;

public enum ERegressionPerformanceMeasure implements IDeterministicPredictionPerformanceMeasure<Double, IRegressionPrediction> {
	MSE(new MeanSquaredError()), RMSE(new RootMeanSquaredError()), RMSLE(new RootMeanSquaredLogarithmError()), MAE(new MeanAbsoluteError()), MAPE(new MeanAbsolutePercentageError()), R2(new R2());

	private final IDeterministicPredictionPerformanceMeasure<Double, IRegressionPrediction> measure;

	private ERegressionPerformanceMeasure(final IDeterministicPredictionPerformanceMeasure<Double, IRegressionPrediction> measure) {
		this.measure = measure;
	}

	@Override
	public double loss(final List<? extends Double> expected, final List<? extends IRegressionPrediction> predicted) {
		return this.measure.loss(expected, predicted);
	}

	@Override
	public double score(final List<? extends Double> expected, final List<? extends IRegressionPrediction> predicted) {
		return this.measure.score(expected, predicted);
	}

	@Override
	public double loss(final IPredictionAndGroundTruthTable<? extends Double, ? extends IRegressionPrediction> pairTable) {
		return this.measure.loss(pairTable);
	}

	@Override
	public double score(final IPredictionAndGroundTruthTable<? extends Double, ? extends IRegressionPrediction> pairTable) {
		return this.measure.score(pairTable);
	}
}

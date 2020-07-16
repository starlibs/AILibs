package ai.libs.jaicore.ml.weka.regression.learner;

import java.util.List;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;
import org.api4.java.ai.ml.regression.evaluation.IRegressionResultBatch;

import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPrediction;
import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPredictionBatch;
import ai.libs.jaicore.ml.weka.classification.learner.AWekaLearner;
import weka.classifiers.Classifier;

public class WekaRegressor extends AWekaLearner<IRegressionPrediction, IRegressionResultBatch> {

	public WekaRegressor(final String name, final String... options) {
		super(name, options);
	}

	public WekaRegressor(final Classifier classifier) {
		super(classifier);
	}

	@Override
	protected IRegressionResultBatch getPredictionListAsBatch(final List<IRegressionPrediction> predictionList) {
		return new SingleTargetRegressionPredictionBatch(predictionList);
	}

	@Override
	public IRegressionPrediction predict(final ILabeledInstance xTest) throws PredictionException, InterruptedException {
		try {
			double reg = this.wrappedLearner.classifyInstance(this.getWekaInstance(xTest).getElement());
			return new SingleTargetRegressionPrediction(reg);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new PredictionException("Could not make a prediction since an exception occurred in the wrapped weka classifier.", e);
		}
	}

}

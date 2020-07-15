package ai.libs.jaicore.ml.weka.regression.learner;

import java.util.List;

import org.api4.java.ai.ml.core.dataset.serialization.UnsupportedAttributeTypeException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;
import org.api4.java.ai.ml.regression.evaluation.IRegressionResultBatch;

import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPrediction;
import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPredictionBatch;
import ai.libs.jaicore.ml.weka.classification.learner.AWekaLearner;
import ai.libs.jaicore.ml.weka.dataset.WekaInstance;
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
		if (this.schema == null) {
			throw new IllegalStateException("Cannot conduct predictions with the classifier, because the dataset scheme has not been defined.");
		}
		WekaInstance instance;
		if (xTest instanceof WekaInstance) {
			instance = (WekaInstance) xTest;
		} else {
			try {
				instance = new WekaInstance(this.schema, xTest);
			} catch (UnsupportedAttributeTypeException e) {
				throw new PredictionException("Could not create WekaInstance object from given instance.");
			}
		}

		try {
			double reg = this.wrappedLearner.classifyInstance(instance.getElement());
			return new SingleTargetRegressionPrediction(reg);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new PredictionException("Could not make a prediction since an exception occurred in the wrapped weka classifier.", e);
		}
	}

}

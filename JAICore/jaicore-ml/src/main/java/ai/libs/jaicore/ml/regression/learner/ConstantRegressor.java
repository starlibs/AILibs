package ai.libs.jaicore.ml.regression.learner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;
import org.api4.java.ai.ml.regression.evaluation.IRegressionResultBatch;

import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;
import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPrediction;
import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPredictionBatch;

public class ConstantRegressor extends ASupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>, IPrediction, IPredictionBatch> {

	private Double constantValue;

	public ConstantRegressor() {
	}

	@Override
	public void fit(final ILabeledDataset<? extends ILabeledInstance> dTrain) throws TrainingException, InterruptedException {
		Objects.requireNonNull(dTrain);
		if (dTrain.isEmpty()) {
			throw new IllegalArgumentException("Cannot train majority classifier with empty training set.");
		}
		this.constantValue = dTrain.stream().filter(x -> x.getLabel() != null).mapToDouble(x -> (double) x.getLabel()).average().getAsDouble();
	}

	@Override
	public IRegressionPrediction predict(final ILabeledInstance xTest) throws PredictionException, InterruptedException {
		return new SingleTargetRegressionPrediction(this.constantValue);
	}

	@Override
	public IRegressionResultBatch predict(final ILabeledInstance[] dTest) throws PredictionException, InterruptedException {
		List<IRegressionPrediction> preds = new ArrayList<>(dTest.length);
		for (ILabeledInstance i : dTest) {
			preds.add(this.predict(i));
		}
		return new SingleTargetRegressionPredictionBatch(preds);
	}

}

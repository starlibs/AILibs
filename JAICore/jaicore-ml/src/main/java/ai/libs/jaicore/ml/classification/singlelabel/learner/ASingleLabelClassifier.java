package ai.libs.jaicore.ml.classification.singlelabel.learner;

import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationDataset;
import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationInstance;
import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassificationPrediction;
import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassificationPredictionBatch;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;

import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassificationPredictionBatch;
import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;

public abstract class ASingleLabelClassifier extends ASupervisedLearner<ISingleLabelClassificationInstance, ISingleLabelClassificationDataset> {

	@Override
	public ISingleLabelClassificationPrediction fitAndPredict(final ISingleLabelClassificationDataset dTrain, final ISingleLabelClassificationInstance xTest) throws TrainingException, PredictionException, InterruptedException {
		return (ISingleLabelClassificationPrediction) super.fitAndPredict(dTrain, xTest);
	}

	@Override
	public ISingleLabelClassificationPredictionBatch fitAndPredict(final ISingleLabelClassificationDataset dTrain, final ISingleLabelClassificationInstance[] xTest) throws TrainingException, PredictionException, InterruptedException {
		return (ISingleLabelClassificationPredictionBatch) super.fitAndPredict(dTrain, xTest);
	}

	@Override
	public ISingleLabelClassificationPredictionBatch fitAndPredict(final ISingleLabelClassificationDataset dTrain, final ISingleLabelClassificationDataset dTest) throws TrainingException, PredictionException, InterruptedException {
		return (ISingleLabelClassificationPredictionBatch) super.fitAndPredict(dTrain, dTest);
	}

	@Override
	public ISingleLabelClassificationPredictionBatch predict(final ISingleLabelClassificationDataset dTest) throws PredictionException, InterruptedException {
		return (ISingleLabelClassificationPredictionBatch) super.predict(dTest);
	}

	@Override
	public ISingleLabelClassificationPredictionBatch predict(final ISingleLabelClassificationInstance[] dTest) throws PredictionException, InterruptedException {
		return new SingleLabelClassificationPredictionBatch(super.predict(dTest));
	}
}

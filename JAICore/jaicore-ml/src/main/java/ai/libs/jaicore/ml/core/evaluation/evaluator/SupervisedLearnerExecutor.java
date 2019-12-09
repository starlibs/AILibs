package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.List;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.evaluation.execution.ISupervisedLearnerExecutor;
import org.api4.java.ai.ml.core.evaluation.execution.LearnerExecutionFailedException;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

public class SupervisedLearnerExecutor<D extends ILabeledDataset<? extends ILabeledInstance>> implements ISupervisedLearnerExecutor<D> {

	@Override
	public ILearnerRunReport execute(final ISupervisedLearner<? extends ILabeledInstance, D> learner, final D train, final D test) throws LearnerExecutionFailedException {
		long startTrainTime = System.currentTimeMillis();
		try {
			learner.fit(train);
		} catch (TrainingException | InterruptedException e) {
			throw new LearnerExecutionFailedException(startTrainTime, System.currentTimeMillis(), e);
		}
		long endTrainTime = System.currentTimeMillis();
		try {
			return this.getReportForTrainedLearner(learner, train, test, startTrainTime, endTrainTime);
		} catch (PredictionException | InterruptedException e) {
			throw new LearnerExecutionFailedException(startTrainTime, endTrainTime, endTrainTime, System.currentTimeMillis(), e);
		}
	}

	@Override
	public ILearnerRunReport execute(final ISupervisedLearner<? extends ILabeledInstance, D> learner, final D test) throws LearnerExecutionFailedException {
		long startTestTime = System.currentTimeMillis();
		try {
			return this.getReportForTrainedLearner(learner, null, test, -1, -1);
		} catch (PredictionException | InterruptedException e) {
			throw new LearnerExecutionFailedException(-1, -1, startTestTime, System.currentTimeMillis(), e);
		}
	}

	private ILearnerRunReport getReportForTrainedLearner(final ISupervisedLearner<? extends ILabeledInstance, D> learner, final D train, final D test, final long trainingStartTime, final long trainingEndTime) throws PredictionException, InterruptedException {
		long start = System.currentTimeMillis();
		List<? extends IPrediction> predictions = learner.predict(test).getPredictions();
		long endTestTime = System.currentTimeMillis();

		/* create difference table */
		int numTestInstances = test.size();
		PredictionDiff<Object> diff = new PredictionDiff<>();
		for (int j = 0; j < numTestInstances; j++) {
			Object prediction = predictions.get(j).getPrediction();
			Object groundTruth = test.get(j).getLabel();
			if (prediction.getClass() != groundTruth.getClass()) {
				throw new IllegalStateException("Type of ground truth " + groundTruth + " (" + groundTruth.getClass().getName() + ") and prediction " + prediction +" (" + prediction.getClass().getName() + ") do not match!");
			}
			diff.addPair(prediction, groundTruth);
		}
		return new LearnerRunReport(train, test, trainingStartTime, trainingEndTime, start, endTestTime, diff);
	}
}

package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.List;

import org.api4.java.ai.ml.classification.execution.ILearnerRunReport;
import org.api4.java.ai.ml.classification.execution.ISupervisedLearnerExecutor;
import org.api4.java.ai.ml.classification.execution.LearnerExecutionFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

public class SupervisedLearnerExecutor<D extends ILabeledDataset<?>> implements ISupervisedLearnerExecutor<D> {

	@Override
	public ILearnerRunReport execute(final ISupervisedLearner<?, D> learner, final D train, final D test) throws LearnerExecutionFailedException {
		try {
			long startTrainTime = System.currentTimeMillis();
			learner.fit(train);
			long endTrainTime = System.currentTimeMillis();
			return this.getReportForTrainedLearner(learner, test, (int) (endTrainTime - startTrainTime));
		} catch (PredictionException | InterruptedException | TrainingException e) {
			throw new LearnerExecutionFailedException(e);
		}
	}

	@Override
	public ILearnerRunReport execute(final ISupervisedLearner<?, D> learner, final D test) throws LearnerExecutionFailedException {
		try {
			return this.getReportForTrainedLearner(learner, test, -1);
		} catch (PredictionException | InterruptedException e) {
			throw new LearnerExecutionFailedException(e);
		}
	}

	private ILearnerRunReport getReportForTrainedLearner(final ISupervisedLearner<?, D> learner, final D test, final int trainingTime) throws PredictionException, InterruptedException {
		long start = System.currentTimeMillis();
		List<?> predictions = learner.predict(test).getPredictions();
		long endTestTime = System.currentTimeMillis();

		/* create difference table */
		int numTestInstances = test.size();
		PredictionDiff<Object> diff = new PredictionDiff<>();
		for (int j = 0; j < numTestInstances; j++) {
			diff.addPair(predictions.get(j), test.get(j).getLabel());
		}
		return new LearnerRunReport(trainingTime, (int) (endTestTime - start), diff);
	}
}

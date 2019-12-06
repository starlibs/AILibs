package ai.libs.jaicore.ml.core.evaluation.evaluator.events;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.common.event.IEvent;

public class TrainTestSplitEvaluationFailedEvent implements IEvent {
	private final ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> learner;
	private final ILabeledDataset<?> train;
	private final ILabeledDataset<?> test;
	private final Exception exception;
	private final int trainingTimeBeforeException;

	public TrainTestSplitEvaluationFailedEvent(final ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> learner, final ILabeledDataset<?> train, final ILabeledDataset<?> test, final Exception exception,
			final int trainingTimeBeforeException) {
		super();
		this.learner = learner;
		this.train = train;
		this.test = test;
		this.exception = exception;
		this.trainingTimeBeforeException = trainingTimeBeforeException;
	}

	public ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> getLearner() {
		return this.learner;
	}

	public ILabeledDataset<?> getTrain() {
		return this.train;
	}

	public ILabeledDataset<?> getTest() {
		return this.test;
	}

	public Exception getException() {
		return this.exception;
	}

	public int getTrainingTimeBeforeException() {
		return this.trainingTimeBeforeException;
	}
}

package ai.libs.jaicore.ml.core.evaluation.evaluator.events;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.common.event.IEvent;

public class TrainTestSplitEvaluationCompletedEvent implements IEvent {

	private final ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> learner;
	private final ILearnerRunReport report;

	public TrainTestSplitEvaluationCompletedEvent(final ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> learner, final ILearnerRunReport report) {
		super();
		this.learner = learner;
		this.report = report;
	}

	public ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> getLearner() {
		return this.learner;
	}

	public ILearnerRunReport getReport() {
		return this.report;
	}
}

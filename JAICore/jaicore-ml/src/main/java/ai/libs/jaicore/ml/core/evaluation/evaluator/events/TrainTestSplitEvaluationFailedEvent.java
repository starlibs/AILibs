package ai.libs.jaicore.ml.core.evaluation.evaluator.events;

import java.util.Arrays;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.common.event.IEvent;

public class TrainTestSplitEvaluationFailedEvent<I extends ILabeledInstance, D extends ILabeledDataset<? extends I>> implements IEvent {
	private final ISupervisedLearner<I, D> learner;
	private final List<ILearnerRunReport> report;

	public TrainTestSplitEvaluationFailedEvent(final ISupervisedLearner<I, D> learner, final ILearnerRunReport report) {
		this(learner, Arrays.asList(report));
	}

	public TrainTestSplitEvaluationFailedEvent(final ISupervisedLearner<I, D> learner, final List<ILearnerRunReport> report) {
		super();
		this.learner = learner;
		this.report = report;
	}

	public ISupervisedLearner<I, D> getLearner() {
		return this.learner;
	}

	public ILearnerRunReport getLastReport() {
		return this.report.get(this.report.size() - 1);
	}

	public ILearnerRunReport getFirstReport() {
		return this.report.get(0);
	}

	public int getNumReports() {
		return this.report.size();
	}

	public List<ILearnerRunReport> getReportList() {
		return this.report;
	}

	@Override
	public long getTimestamp() {
		throw new UnsupportedOperationException();
	}
}

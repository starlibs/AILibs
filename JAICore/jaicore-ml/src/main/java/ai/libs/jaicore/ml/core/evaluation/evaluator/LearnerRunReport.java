package ai.libs.jaicore.ml.core.evaluation.evaluator;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;

public class LearnerRunReport implements ILearnerRunReport {

	private final ILabeledDataset<?> trainSet;
	private final ILabeledDataset<?> testSet;
	private final long trainStartTime;
	private final long trainEndTime;
	private final long testStartTime;
	private final long testEndTime;
	private final Throwable exception;
	private final IPredictionAndGroundTruthTable diff;

	public LearnerRunReport(final ILabeledDataset<?> trainSet, final ILabeledDataset<?> testSet, final long trainStartTime, final long trainEndTime, final Throwable exception) {
		super();
		this.trainSet = trainSet;
		this.testSet = testSet;
		this.trainStartTime = trainStartTime;
		this.trainEndTime = trainEndTime;
		this.testStartTime = -1;
		this.testEndTime = -1;
		this.diff = null;
		this.exception = exception;
	}

	public LearnerRunReport(final ILabeledDataset<?> trainSet, final ILabeledDataset<?> testSet, final long trainStartTime, final long trainEndTime, final long testStartTime, final long testEndTime, final Throwable exception) {
		super();
		this.trainSet = trainSet;
		this.testSet = testSet;
		this.trainStartTime = trainStartTime;
		this.trainEndTime = trainEndTime;
		this.testStartTime = testStartTime;
		this.testEndTime = testEndTime;
		this.diff = null;
		this.exception = exception;
	}

	public LearnerRunReport(final ILabeledDataset<?> trainSet, final ILabeledDataset<?> testSet, final long trainStartTime, final long trainEndTime, final long testStartTime, final long testEndTime, final IPredictionAndGroundTruthTable diff) {
		super();
		this.trainSet = trainSet;
		this.testSet = testSet;
		this.trainStartTime = trainStartTime;
		this.trainEndTime = trainEndTime;
		this.testStartTime = testStartTime;
		this.testEndTime = testEndTime;
		this.diff = diff;
		this.exception = null;
	}

	@Override
	public long getTrainStartTime() {
		return this.trainStartTime;
	}

	@Override
	public long getTrainEndTime() {
		return this.trainEndTime;
	}

	@Override
	public long getTestStartTime() {
		return this.testStartTime;
	}

	@Override
	public long getTestEndTime() {
		return this.testEndTime;
	}

	@Override
	public ILabeledDataset<?> getTrainSet() {
		return this.trainSet;
	}

	@Override
	public ILabeledDataset<?> getTestSet() {
		return this.testSet;
	}

	@Override
	public IPredictionAndGroundTruthTable getPredictionDiffList() {
		return this.diff;
	}

	@Override
	public Throwable getException() {
		return this.exception;
	}
}

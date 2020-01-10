package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.HashMap;
import java.util.Map;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;

import ai.libs.jaicore.logging.ToJSONStringUtil;

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

	public LearnerRunReport(final ILabeledDataset<?> trainSet, final ILabeledDataset<?> testSet, final long trainStartTime, final long trainEndTime, final long testStartTime, final long testEndTime,
			final IPredictionAndGroundTruthTable diff) {
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

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("trainStartTime", this.trainStartTime);
		fields.put("trainEndTime", this.trainEndTime);
		fields.put("testStartTime", this.testStartTime);
		fields.put("testEndTime", this.testEndTime);
		fields.put("exception", this.exception);
		fields.put("diffClass", this.diff.getClass().getName());
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
}

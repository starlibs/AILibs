package ai.libs.jaicore.ml.core.evaluation.evaluator;

import org.api4.java.ai.ml.core.evaluation.IPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;

public class LearnerRunReport implements ILearnerRunReport {

	private final int trainTime;
	private final int testTime;
	private final IPredictionAndGroundTruthTable diff;

	public LearnerRunReport(final int trainTime, final int testTime, final IPredictionAndGroundTruthTable diff) {
		super();
		this.trainTime = trainTime;
		this.testTime = testTime;
		this.diff = diff;
	}

	@Override
	public int getTrainingTimeInMS() {
		return this.trainTime;
	}

	@Override
	public int getTestTimeInMS() {
		return this.testTime;
	}

	@Override
	public String getTrainSetDescriptor() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getTestSetDescriptor() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IPredictionAndGroundTruthTable getPredictionDiffList() {
		return this.diff;
	}
}

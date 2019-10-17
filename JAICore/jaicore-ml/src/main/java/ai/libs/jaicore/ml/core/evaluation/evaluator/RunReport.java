package ai.libs.jaicore.ml.core.evaluation.evaluator;

import org.api4.java.ai.ml.classification.execution.IClassificationPredictionAndGroundTruthTable;
import org.api4.java.ai.ml.classification.execution.IClassifierRunReport;

public class RunReport implements IClassifierRunReport {

	private final int trainTime;
	private final int testTime;
	private final IClassificationPredictionAndGroundTruthTable diff;

	public RunReport(final int trainTime, final int testTime, final IClassificationPredictionAndGroundTruthTable diff) {
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
	public IClassificationPredictionAndGroundTruthTable getPredictionDiffList() {
		return this.diff;
	}
}

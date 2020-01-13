package ai.libs.jaicore.ml.core.evaluation.evaluator.events;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.common.event.IEvent;

public class MCCVSplitEvaluationEvent implements IEvent {
	private final IClassifier classifier;
	private final int numInstancesUsedForTraining;
	private final int numInstancesUsedForValidation;
	private final int splitEvaluationTime;
	private final double observedScore;

	public MCCVSplitEvaluationEvent(final IClassifier classifier, final int numInstancesUsedForTraining, final int numInstancesUsedForValidation, final int splitEvaluationTime, final double observedScore) {
		super();
		this.classifier = classifier;
		this.numInstancesUsedForTraining = numInstancesUsedForTraining;
		this.numInstancesUsedForValidation = numInstancesUsedForValidation;
		this.splitEvaluationTime = splitEvaluationTime;
		this.observedScore = observedScore;
	}

	public IClassifier getClassifier() {
		return this.classifier;
	}

	public int getSplitEvaluationTime() {
		return this.splitEvaluationTime;
	}

	public double getObservedScore() {
		return this.observedScore;
	}

	public int getNumInstancesUsedForTraining() {
		return this.numInstancesUsedForTraining;
	}

	public int getNumInstancesUsedForValidation() {
		return this.numInstancesUsedForValidation;
	}

	@Override
	public long getTimestamp() {
		throw new UnsupportedOperationException();
	}
}

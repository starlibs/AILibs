package jaicore.ml.evaluation.evaluators.weka.events;

import jaicore.basic.events.IEvent;
import weka.classifiers.Classifier;

public class MCCVSplitEvaluationEvent implements IEvent {
	private final Classifier classifier;
	private final int numInstancesUsedForTraining;
	private final int numInstancesUsedForValidation;
	private final int splitEvaluationTime;
	private final double observedScore;

	public MCCVSplitEvaluationEvent(final Classifier classifier, final int numInstancesUsedForTraining, final int numInstancesUsedForValidation, final int splitEvaluationTime, final double observedScore) {
		super();
		this.classifier = classifier;
		this.numInstancesUsedForTraining = numInstancesUsedForTraining;
		this.numInstancesUsedForValidation = numInstancesUsedForValidation;
		this.splitEvaluationTime = splitEvaluationTime;
		this.observedScore = observedScore;
	}

	public Classifier getClassifier() {
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

}

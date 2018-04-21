package jaicore.ml.evaluation;

import weka.classifiers.Classifier;

public class ClassifierMeasurementEvent<V> {
	private final Classifier classifier;
	private final Throwable error;
	private final V score;

	public ClassifierMeasurementEvent(Classifier c, V score, Throwable e) {
		super();
		this.classifier = c;
		this.score = score;
		this.error = e;
	}

	public Classifier getClassifier() {
		return classifier;
	}

	public V getScore() {
		return score;
	}

	public Throwable getError() {
		return error;
	}
}

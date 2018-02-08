package de.upb.crc901.mlplan.search.evaluators;

import weka.classifiers.Classifier;

public class ClassifierMeasurementEvent<V> {
	private final Classifier classifier;
	private final V score;

	public ClassifierMeasurementEvent(Classifier c, V score) {
		super();
		this.classifier = c;
		this.score = score;
	}

	public Classifier getClassifier() {
		return classifier;
	}

	public V getScore() {
		return score;
	}

}

package de.upb.crc901.mlplan.core.events;

import hasco.model.ComponentInstance;
import jaicore.basic.events.IEvent;
import weka.classifiers.Classifier;

public class ClassifierCreatedEvent implements IEvent {
	private final ComponentInstance instance;
	private final Classifier classifier;

	public ClassifierCreatedEvent(final ComponentInstance instance, final Classifier classifier) {
		super();
		this.instance = instance;
		this.classifier = classifier;
	}

	public ComponentInstance getInstance() {
		return this.instance;
	}

	public Classifier getClassifier() {
		return this.classifier;
	}
}

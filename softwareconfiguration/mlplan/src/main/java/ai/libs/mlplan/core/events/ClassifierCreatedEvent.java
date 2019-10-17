package ai.libs.mlplan.core.events;

import org.api4.java.ai.ml.classification.IClassifier;

import ai.libs.hasco.model.ComponentInstance;
import ai.libs.jaicore.basic.events.IEvent;

public class ClassifierCreatedEvent implements IEvent {
	private final ComponentInstance instance;
	private final IClassifier<?, ?> classifier;

	public ClassifierCreatedEvent(final ComponentInstance instance, final IClassifier<?, ?> classifier) {
		super();
		this.instance = instance;
		this.classifier = classifier;
	}

	public ComponentInstance getInstance() {
		return this.instance;
	}

	public IClassifier<?, ?> getClassifier() {
		return this.classifier;
	}
}

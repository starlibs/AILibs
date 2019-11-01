package ai.libs.mlplan.core.events;

import org.api4.java.ai.ml.core.learner.ISupervisedLearner;

import ai.libs.hasco.model.ComponentInstance;
import ai.libs.jaicore.basic.events.IEvent;

public class SupervisedLearnerCreatedEvent implements IEvent {
	private final ComponentInstance instance;
	private final ISupervisedLearner<?, ?> classifier;

	public SupervisedLearnerCreatedEvent(final ComponentInstance instance, final ISupervisedLearner<?, ?> classifier) {
		super();
		this.instance = instance;
		this.classifier = classifier;
	}

	public ComponentInstance getInstance() {
		return this.instance;
	}

	public ISupervisedLearner<?, ?> getClassifier() {
		return this.classifier;
	}
}

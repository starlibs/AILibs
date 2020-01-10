package ai.libs.mlplan.core.events;

import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.common.event.IEvent;

import ai.libs.hasco.model.ComponentInstance;

public class SupervisedLearnerCreatedEvent implements IEvent {
	private final ComponentInstance instance;
	private final ISupervisedLearner<?, ?> classifier;
	private final long timestamp = System.currentTimeMillis();

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

	@Override
	public long getTimestamp() {
		return this.timestamp;
	}
}

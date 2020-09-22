package ai.libs.mlplan.core.events;

import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.common.event.IEvent;

import ai.libs.jaicore.components.api.IComponentInstance;

public class SupervisedLearnerCreatedEvent implements IEvent {
	private final IComponentInstance instance;
	private final ISupervisedLearner<?, ?> classifier;
	private final long timestamp = System.currentTimeMillis();

	public SupervisedLearnerCreatedEvent(final IComponentInstance instance, final ISupervisedLearner<?, ?> classifier) {
		super();
		this.instance = instance;
		this.classifier = classifier;
	}

	public IComponentInstance getInstance() {
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

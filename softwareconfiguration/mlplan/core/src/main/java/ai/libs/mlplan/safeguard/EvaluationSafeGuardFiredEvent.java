package ai.libs.mlplan.safeguard;

import org.api4.java.common.event.IEvent;

import ai.libs.jaicore.components.api.IComponentInstance;

public class EvaluationSafeGuardFiredEvent implements IEvent {

	private final IComponentInstance ci;
	private final long timestamp;

	public EvaluationSafeGuardFiredEvent(final IComponentInstance ci) {
		this.timestamp = System.currentTimeMillis();
		this.ci = ci;
	}

	public IComponentInstance getComponentInstance() {
		return this.ci;
	}

	@Override
	public long getTimestamp() {
		return this.timestamp;
	}

}

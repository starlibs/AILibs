package ai.libs.mlplan.safeguard;

import org.api4.java.common.event.IEvent;

import ai.libs.hasco.model.ComponentInstance;

public class EvaluationSafeGuardFiredEvent implements IEvent {

	private final ComponentInstance ci;
	private final long timestamp;

	public EvaluationSafeGuardFiredEvent(final ComponentInstance ci) {
		this.timestamp = System.currentTimeMillis();
		this.ci = ci;
	}

	public ComponentInstance getComponentInstance() {
		return this.ci;
	}

	@Override
	public long getTimestamp() {
		return this.timestamp;
	}

}

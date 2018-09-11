package jaicore.concurrent;

import java.util.TimerTask;

public abstract class NamedTimerTask extends TimerTask {
	private String descriptor;

	public NamedTimerTask(String descriptor) {
		super();
		this.descriptor = descriptor;
	}

	public String getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}
}

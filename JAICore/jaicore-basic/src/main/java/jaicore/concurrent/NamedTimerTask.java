package jaicore.concurrent;

import java.util.TimerTask;

public abstract class NamedTimerTask extends TimerTask {
	private String descriptor;

	public NamedTimerTask(final String descriptor) {
		super();
		this.descriptor = descriptor;
	}

	public String getDescriptor() {
		return this.descriptor;
	}

	public void setDescriptor(final String descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public String toString() {
		return "NamedTimerTask: " + this.descriptor;
	}
}

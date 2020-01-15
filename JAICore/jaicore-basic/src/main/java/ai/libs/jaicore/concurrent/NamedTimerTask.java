package ai.libs.jaicore.concurrent;

public abstract class NamedTimerTask extends TrackableTimerTask {

	private String descriptor;

	public NamedTimerTask() {
		this("<unnamed task>");
	}

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
		return "NamedTimerTask: " + this.descriptor + ", canceled: " + this.isCanceled();
	}
}

package ai.libs.jaicore.concurrent;

public abstract class ANamedTimerTask extends TrackableTimerTask {

	private String descriptor;

	protected ANamedTimerTask() {
		this("<unnamed task>");
	}

	protected ANamedTimerTask(final String descriptor) {
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

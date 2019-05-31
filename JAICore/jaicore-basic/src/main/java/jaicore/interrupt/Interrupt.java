package jaicore.interrupt;

public class Interrupt {
	private final Thread interruptingThread;
	private final Thread interruptedThread;
	private final long timestampOfInterruption;
	private final Object reasonForInterruption;

	public Interrupt(final Thread interruptingThread, final Thread interruptedThread, final long timestampOfInterruption, final Object reasonForInterruption) {
		super();
		this.interruptingThread = interruptingThread;
		this.interruptedThread = interruptedThread;
		this.timestampOfInterruption = timestampOfInterruption;
		this.reasonForInterruption = reasonForInterruption;
	}

	public Thread getInterruptingThread() {
		return this.interruptingThread;
	}

	public Thread getInterruptedThread() {
		return this.interruptedThread;
	}

	public long getTimestampOfInterruption() {
		return this.timestampOfInterruption;
	}

	public Object getReasonForInterruption() {
		return this.reasonForInterruption;
	}
}

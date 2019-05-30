package jaicore.interrupt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.concurrent.NamedTimerTask;

public class InterruptionTimerTask extends NamedTimerTask {
	private static final Logger logger = LoggerFactory.getLogger(InterruptionTimerTask.class);
	private final Thread threadToBeInterrupted;
	private final Object reason;
	private final Runnable hookToExecutePriorToInterruption;
	private boolean triggered = false;
	private boolean finished = false;

	public InterruptionTimerTask(final String descriptor, final Thread threadToBeInterrupted, final Object reason, final Runnable hookToExecutePriorToInterruption) {
		super(descriptor);
		this.threadToBeInterrupted = threadToBeInterrupted;
		this.hookToExecutePriorToInterruption = hookToExecutePriorToInterruption;
		this.reason = reason;
	}

	public InterruptionTimerTask(final String descriptor, final Thread threadToBeInterrupted, final Runnable hookToExecutePriorToInterruption) {
		super(descriptor);
		this.threadToBeInterrupted = threadToBeInterrupted;
		this.hookToExecutePriorToInterruption = hookToExecutePriorToInterruption;
		reason = this;
	}

	public InterruptionTimerTask(final String descriptor, final Thread threadToBeInterrupted) {
		this(descriptor, threadToBeInterrupted, null);
	}

	public InterruptionTimerTask(final String descriptor, final Runnable hookToExecutePriorToInterruption) {
		this(descriptor, Thread.currentThread(), hookToExecutePriorToInterruption);
	}

	public InterruptionTimerTask(final String descriptor) {
		this(descriptor, Thread.currentThread(), null);
	}

	public Thread getThreadToBeInterrupted() {
		return threadToBeInterrupted;
	}

	public Runnable getHookToExecutePriorToInterruption() {
		return hookToExecutePriorToInterruption;
	}

	@Override
	public void run() {
		long delay = System.currentTimeMillis() - scheduledExecutionTime();
		triggered = true;
		logger.info("Executing interruption task {} with descriptor \"{}\". Interrupting thread {}. This interrupt has been triggered with a delay of {}ms", hashCode(), getDescriptor(), threadToBeInterrupted, delay);
		if (delay > 50) {
			logger.warn("Interrupt is executed with a delay of {}ms", delay);
		}
		if (hookToExecutePriorToInterruption != null) {
			logger.debug("Running pre-interruption hook");
			hookToExecutePriorToInterruption.run();
		} else {
			logger.debug("No pre-interruption hook has been defined.");
		}
		logger.debug("Interrupting the thread.");
		Interrupter.get().interruptThread(threadToBeInterrupted, reason);
		finished = true;
	}

	public boolean isTriggered() {
		return triggered;
	}

	public boolean isFinished() {
		return finished;
	}
}

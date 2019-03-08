package jaicore.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.interrupt.Interrupter;

public class InterruptionTimerTask extends NamedTimerTask {
	private static final Logger logger = LoggerFactory.getLogger(InterruptionTimerTask.class);
	private final Thread threadToBeInterrupted;
	private final Object reason;
	private final Runnable hookToExecutePriorToInterruption;

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
		this.reason = this;
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
		return this.threadToBeInterrupted;
	}

	public Runnable getHookToExecutePriorToInterruption() {
		return this.hookToExecutePriorToInterruption;
	}

	@Override
	public void run() {
		if (this.hookToExecutePriorToInterruption != null) {
			logger.info("Executing pre-interruption hook.");
			this.hookToExecutePriorToInterruption.run();
		}
		logger.info("Executing interruption task with descriptor \"{}\". Interrupting thread {}", this.getDescriptor(), this.threadToBeInterrupted);
		Interrupter.get().interruptThread(this.threadToBeInterrupted, this.reason);
	}
	
	
}

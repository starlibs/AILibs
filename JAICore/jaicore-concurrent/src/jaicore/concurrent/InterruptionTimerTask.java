package jaicore.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterruptionTimerTask extends NamedTimerTask {
	private static final Logger logger = LoggerFactory.getLogger(InterruptionTimerTask.class);
	private final Thread threadToBeInterrupted;
	private final Runnable hookToExecutePriorToInterruption;

	public InterruptionTimerTask(String descriptor, Thread threadToBeInterrupted, Runnable hookToExecutePriorToInterruption) {
		super(descriptor);
		this.threadToBeInterrupted = threadToBeInterrupted;
		this.hookToExecutePriorToInterruption = hookToExecutePriorToInterruption;
	}

	public InterruptionTimerTask(String descriptor, Thread threadToBeInterrupted) {
		this(descriptor, threadToBeInterrupted, null);
	}

	public InterruptionTimerTask(String descriptor, Runnable hookToExecutePriorToInterruption) {
		this(descriptor, Thread.currentThread(), hookToExecutePriorToInterruption);
	}

	public InterruptionTimerTask(String descriptor) {
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
		if (hookToExecutePriorToInterruption != null) {
			logger.info("Executing pre-interruption hook.");
			hookToExecutePriorToInterruption.run();
		}
		logger.info("Executing interruption task with descriptor \"{}\". Interrupting thread {}", getDescriptor(), threadToBeInterrupted);
		threadToBeInterrupted.interrupt();
	}
}

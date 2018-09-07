package jaicore.concurrent;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterruptionTimerTask extends TimerTask {
	private static final Logger logger = LoggerFactory.getLogger(InterruptionTimerTask.class);
	private final Thread threadToBeInterrupted;
	private final Runnable hookToExecutePriorToInterruption;
	public InterruptionTimerTask(Thread threadToBeInterrupted, Runnable hookToExecutePriorToInterruption) {
		super();
		this.threadToBeInterrupted = threadToBeInterrupted;
		this.hookToExecutePriorToInterruption = hookToExecutePriorToInterruption;
	}
	
	public InterruptionTimerTask(Thread threadToBeInterrupted) {
		this (threadToBeInterrupted, null);
	}

	public InterruptionTimerTask(Runnable hookToExecutePriorToInterruption) {
		this(Thread.currentThread(), hookToExecutePriorToInterruption);
	}
	
	public InterruptionTimerTask() {
		this(Thread.currentThread(), null);
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
		logger.info("interrupting thread {}", threadToBeInterrupted);
		threadToBeInterrupted.interrupt();
	}
}

package jaicore.concurrent;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.Cancelable;

public class CancellationTimerTask extends TimerTask {
	private static final Logger logger = LoggerFactory.getLogger(InterruptionTimerTask.class);
	private final Cancelable thingToBeCanceled;
	private final Runnable hookToExecutePriorToCancel;
	public CancellationTimerTask(Cancelable cancelable, Runnable hookToExecutePriorToInterruption) {
		super();
		this.thingToBeCanceled = cancelable;
		this.hookToExecutePriorToCancel = hookToExecutePriorToInterruption;
	}
	
	public CancellationTimerTask(Cancelable thingToBeCanceled) {
		this (thingToBeCanceled, null);
	}

	public Cancelable getCancelable() {
		return thingToBeCanceled;
	}

	public Runnable getHookToExecutePriorToInterruption() {
		return hookToExecutePriorToCancel;
	}

	@Override
	public void run() {
		if (hookToExecutePriorToCancel != null) {
			logger.info("Executing pre-interruption hook.");
			hookToExecutePriorToCancel.run();
		}
		logger.info("Canceling {}", thingToBeCanceled);
		thingToBeCanceled.cancel();
	}
}

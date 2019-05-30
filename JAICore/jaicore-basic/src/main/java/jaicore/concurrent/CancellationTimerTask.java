package jaicore.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.Cancelable;
import jaicore.interrupt.InterruptionTimerTask;

public class CancellationTimerTask extends NamedTimerTask {
	private static final Logger logger = LoggerFactory.getLogger(InterruptionTimerTask.class);
	private final Cancelable thingToBeCanceled;
	private final Runnable hookToExecutePriorToCancel;

	public CancellationTimerTask(String descriptor, Cancelable cancelable, Runnable hookToExecutePriorToInterruption) {
		super(descriptor);
		this.thingToBeCanceled = cancelable;
		this.hookToExecutePriorToCancel = hookToExecutePriorToInterruption;
	}

	public CancellationTimerTask(String descriptor, Cancelable thingToBeCanceled) {
		this(descriptor, thingToBeCanceled, null);
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
		logger.info("Executing cancel task {}. Canceling {}", getDescriptor(), thingToBeCanceled);
		thingToBeCanceled.cancel();
	}
}

package ai.libs.jaicore.concurrent;

import org.api4.java.common.control.Cancelable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CancellationTimerTask extends NamedTimerTask {
	private static final Logger logger = LoggerFactory.getLogger(CancellationTimerTask.class);
	private final Cancelable thingToBeCanceled;
	private final Runnable hookToExecutePriorToCancel;

	public CancellationTimerTask(final String descriptor, final Cancelable cancelable, final Runnable hookToExecutePriorToInterruption) {
		super(descriptor);
		this.thingToBeCanceled = cancelable;
		this.hookToExecutePriorToCancel = hookToExecutePriorToInterruption;
	}

	public CancellationTimerTask(final String descriptor, final Cancelable thingToBeCanceled) {
		this(descriptor, thingToBeCanceled, null);
	}

	public Cancelable getCancelable() {
		return this.thingToBeCanceled;
	}

	public Runnable getHookToExecutePriorToInterruption() {
		return this.hookToExecutePriorToCancel;
	}

	@Override
	public void run() {
		if (this.hookToExecutePriorToCancel != null) {
			logger.info("Executing pre-interruption hook.");
			this.hookToExecutePriorToCancel.run();
		}
		logger.info("Executing cancel task {}. Canceling {}", this.getDescriptor(), this.thingToBeCanceled);
		this.thingToBeCanceled.cancel();
	}
}

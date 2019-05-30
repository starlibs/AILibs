package jaicore.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadGroupObserver extends Thread {
	private static final Logger L = LoggerFactory.getLogger(ThreadGroupObserver.class);

	private final ThreadGroup group;
	private int maxObservedThreads = 0;
	private boolean active = true;
	private final int maxAllowedThreads;
	private final Runnable hookOnConstraintViolation;
	private Thread[] threadsAtPointOfViolation;

	public ThreadGroupObserver(final ThreadGroup group, final int maxAllowedThreads, final Runnable hookOnConstraintViolation) {
		super("ThreadGroupObserver-" + hookOnConstraintViolation);
		this.group = group;
		this.maxAllowedThreads = maxAllowedThreads;
		this.hookOnConstraintViolation = hookOnConstraintViolation;
		if (maxAllowedThreads <= 0) {
			this.active = false;
		}
	}

	public void cancel() {
		this.active = false;
		this.interrupt(); // no controlled interrupt in run method
	}

	@Override
	public void run() {
		while (this.active && !Thread.currentThread().isInterrupted()) {
			this.maxObservedThreads = Math.max(this.maxObservedThreads, this.group.activeCount());
			if (this.isThreadConstraintViolated()) {
				/* store all currently active threads */
				this.threadsAtPointOfViolation = new Thread[this.group.activeCount()];
				this.group.enumerate(this.threadsAtPointOfViolation, true);
				L.info("Running violation hook!");
				this.hookOnConstraintViolation.run();
				return;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // no controlled interrupt needed, because execution will cease after this anyway
				return;
			}
		}
	}

	public int getMaxObservedThreads() {
		return this.maxObservedThreads;
	}

	public boolean isThreadConstraintViolated() {
		return this.maxAllowedThreads > 0 && this.maxObservedThreads > this.maxAllowedThreads;
	}

	public Thread[] getThreadsAtPointOfViolation() {
		return this.threadsAtPointOfViolation;
	}
}

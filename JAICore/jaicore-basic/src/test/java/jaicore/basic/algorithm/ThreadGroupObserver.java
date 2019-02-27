package jaicore.basic.algorithm;

import java.util.ArrayList;
import java.util.Collection;

public class ThreadGroupObserver extends Thread {
	private final String name;
	private final ThreadGroup group;
	private int maxObservedThreads = 0;
	private boolean active = true;
	private final int maxAllowedThreads;
	private final Runnable hookOnConstraintViolation;
	private Thread[] threadsAtPointOfViolation;
	
	public ThreadGroupObserver(ThreadGroup group, int maxAllowedThreads, Runnable hookOnConstraintViolation) {
		super();
		this.name = "Observer of ThreadGroup " + group;
		this.group = group;
		this.maxAllowedThreads = maxAllowedThreads;
		this.hookOnConstraintViolation = hookOnConstraintViolation;
	}

	public void cancel() {
		active = false;
		this.interrupt();
	}

	@Override
	public void run() {
		while (active && !Thread.currentThread().isInterrupted()) {
			maxObservedThreads = Math.max(maxObservedThreads, group.activeCount());
			if (isThreadConstraintViolated()) {
				
				/* store all currently active threads */
				threadsAtPointOfViolation = new Thread[group.activeCount()];
				group.enumerate(threadsAtPointOfViolation, true);
				
				System.out.println("Running violation hook!");
				hookOnConstraintViolation.run();
				return;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	public int getMaxObservedThreads() {
		return maxObservedThreads;
	}
	
	public boolean isThreadConstraintViolated() {
		return maxAllowedThreads > 0 && maxObservedThreads > maxAllowedThreads;
	}

	public Thread[] getThreadsAtPointOfViolation() {
		return threadsAtPointOfViolation;
	}
}

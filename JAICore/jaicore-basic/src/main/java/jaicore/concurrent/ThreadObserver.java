package jaicore.concurrent;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jaicore.basic.sets.SetUtil;

public class ThreadObserver extends Thread {
	private final List<Thread> threadsThatHaveBeenActiveAtLastObservation = new ArrayList<>();
	private final PrintStream stream;

	public ThreadObserver(final PrintStream stream) {
		super("ThreadObserver");
		this.stream = stream;
	}

	@Override
	public void run() {
		try {
			while (true) {
				List<Thread> currentlyActiveThreads = Thread.getAllStackTraces().keySet().stream()
						.sorted((t1, t2) -> t1.getName().compareTo(t2.getName())).collect(Collectors.toList());
				List<Thread> newThreads = SetUtil.difference(currentlyActiveThreads, this.threadsThatHaveBeenActiveAtLastObservation);
				List<Thread> goneThreads = SetUtil.difference(this.threadsThatHaveBeenActiveAtLastObservation, currentlyActiveThreads);
				if (!newThreads.isEmpty()) {
					this.stream.println("" + System.currentTimeMillis());
					this.stream.println("New Threads:");
					for (Thread t : newThreads) {
						this.stream.println("\t" + t.getName() + ": " + t.getThreadGroup());
					}
				}
				if (!goneThreads.isEmpty()) {
					this.stream.println("Gone Threads:");
					for (Thread t : goneThreads) {
						this.stream.println("\t" + t.getName() + ": " + t.getThreadGroup());
					}
				}
				this.threadsThatHaveBeenActiveAtLastObservation.clear();
				this.threadsThatHaveBeenActiveAtLastObservation.addAll(currentlyActiveThreads);
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt(); // no controlled interrupt necessary, because the execution will cease immediately after this anyway
		}
	}
}

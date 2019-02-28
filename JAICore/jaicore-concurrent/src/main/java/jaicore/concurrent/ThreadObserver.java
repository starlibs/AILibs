package jaicore.concurrent;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jaicore.basic.sets.SetUtil;

public class ThreadObserver extends Thread {
	private final List<Thread> threadsThatHaveBeenActiveAtLastObservation = new ArrayList<>();
	private final PrintStream stream;

	public ThreadObserver(PrintStream stream) {
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
					stream.println("" + System.currentTimeMillis());
					stream.println("New Threads:");
					for (Thread t : newThreads)
						stream.println("\t" + t.getName() + ": " + t.getThreadGroup());
				}
				if (!goneThreads.isEmpty()) {
					stream.println("Gone Threads:");
					for (Thread t : goneThreads)
						stream.println("\t" + t.getName() + ": " + t.getThreadGroup());
				}
				this.threadsThatHaveBeenActiveAtLastObservation.clear();
				this.threadsThatHaveBeenActiveAtLastObservation.addAll(currentlyActiveThreads);
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}

package jaicore.concurrent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeoutTimer extends Timer {
	private static final Logger logger = LoggerFactory.getLogger(TimeoutTimer.class);
	private static final TimeoutTimer instance = new TimeoutTimer();
	private final List<TimeoutSubmitter> emittedSubmitters = new ArrayList<>();
	private final Set<TimerTask> tasks = new HashSet<>();

	private TimeoutTimer() {
		super("Global TimeoutTimer", true);
	}

	public static TimeoutTimer getInstance() {
		return instance;
	}

	public TimeoutSubmitter getSubmitter() {
		return new TimeoutSubmitter();
	}

	@Override
	public void cancel() {
		throw new UnsupportedOperationException("The TimeoutTimer must not be canceled manually!");
	}

	@Override
	public String toString() {
		return this.tasks.toString();
	}

	public class TimeoutSubmitter {
		private TimeoutSubmitter() {
			synchronized (instance) {
				TimeoutTimer.this.emittedSubmitters.add(this);
			}
		}

		public synchronized TimerTask interruptMeAfterMS(final int delay, String reason) {
			logger.info("Scheduling interrupt for thread {} in {}ms", Thread.currentThread(), delay);
			return this.interruptThreadAfterMS(Thread.currentThread(), delay, reason);
		}

		public synchronized TimerTask interruptMeAfterMS(final int delay, String reason, final Runnable preInterruptionHook) {
			logger.info("Scheduling interrupt for thread {} in {}ms", Thread.currentThread(), delay);
			return this.interruptThreadAfterMS(Thread.currentThread(), delay, reason, preInterruptionHook);
		}

		public synchronized TimerTask interruptThreadAfterMS(final Thread thread, final long delay, String reason) {
			return this.interruptThreadAfterMS(thread, delay, reason, null);
		}

		public synchronized TimerTask interruptThreadAfterMS(final Thread thread, final long delay, String reason, final Runnable preInterruptionHook) {
			TimerTask task = new InterruptionTimerTask(reason, thread, preInterruptionHook);
			if (!TimeoutTimer.this.emittedSubmitters.contains(this)) {
				throw new IllegalStateException("Cannot submit interrupt job to submitter " + this + " since it has already been closed!");
			}
			TimeoutTimer.this.schedule(task, delay);

			/* create id for job and return it */
			TimeoutTimer.this.tasks.add(task);
			logger.info("Job {} scheduled for in {}ms.", task, delay);
			return task;
		}

		public void close() {
			synchronized (instance) {
				TimeoutTimer.this.emittedSubmitters.remove(this);
				logger.info("Canceled timer");
			}
		}
	}
}

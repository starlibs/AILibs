package jaicore.concurrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeoutTimer {
	private final static Logger logger = LoggerFactory.getLogger(TimeoutTimer.class);
	private final static TimeoutTimer instance = new TimeoutTimer();
	private Timer timer;
	private final List<TimeoutSubmitter> emittedSubmitters = new ArrayList<>();
	private final Map<Integer, TimerTask> tasks = new HashMap<>();

	private TimeoutTimer() {
	}

	public static TimeoutTimer getInstance() {
		return instance;
	}

	public TimeoutSubmitter getSubmitter() {
		TimeoutSubmitter submitter = new TimeoutSubmitter();
		return submitter;
	}

	public class TimeoutSubmitter {
		private TimeoutSubmitter() {
			synchronized (instance) {
				TimeoutTimer.this.emittedSubmitters.add(this);
			}
		}

		public int interruptMeAfterMS(final long delay) {
			return this.interruptThreadAfterMS(Thread.currentThread(), delay);
		}

		public int interruptThreadAfterMS(final Thread thread, final long delay) {
			return this.scheduleTask(new TimerTask() {
				@Override
				public void run() {
					logger.info("interrupting thread {} after delay {}ms", thread, delay);
					thread.interrupt();
				}
			}, delay);
		}

		public int interruptJobAfterMS(final Future<?> job, final long delay) {
			return this.scheduleTask(new TimerTask() {

				@Override
				public void run() {
					logger.info("canceling job {} after delay {}", job, delay);
					job.cancel(true);
				}
			}, delay);
		}

		public void cancelTimeout(final int taskId) {
			if (!TimeoutTimer.this.tasks.containsKey(taskId)) {
				throw new IllegalArgumentException("Task with id " + taskId + " was not found.");
			}
			TimeoutTimer.this.tasks.get(taskId).cancel();
			TimeoutTimer.this.tasks.remove(taskId);
			logger.info("Timeout for {} has been canceled.", taskId);
		}

		private synchronized int scheduleTask(final TimerTask task, final long delay) {
			synchronized (instance) {
				if (!TimeoutTimer.this.emittedSubmitters.contains(this)) {
					throw new IllegalStateException(
							"Cannot submit interrupt job to submitter " + this + " since it has already been closed!");
				}
				if (TimeoutTimer.this.timer == null) {
					TimeoutTimer.this.timer = new Timer(TimeoutTimer.class.getName() + " - Timer");
					logger.info("Created new timer");
				}
				TimeoutTimer.this.timer.schedule(task, delay);

				/* create id for job and return it */
				int id;
				do {
					id = (int) (Math.random() * 1000000);
				} while (TimeoutTimer.this.tasks.containsKey(id));
				TimeoutTimer.this.tasks.put(id, task);
				logger.info("Job {} scheduled for in {}ms.", id, delay);
				return id;
			}
		}

		public void close() {
			synchronized (instance) {
				TimeoutTimer.this.emittedSubmitters.remove(this);
				if (TimeoutTimer.this.timer != null && TimeoutTimer.this.emittedSubmitters.isEmpty()) {
					TimeoutTimer.this.timer.cancel();
					TimeoutTimer.this.timer = null;
				}
				logger.info("Canceled timer");
			}
		}
	}
}

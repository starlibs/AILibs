package jaicore.concurrent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalTimer extends Timer {
	private static final Logger logger = LoggerFactory.getLogger(GlobalTimer.class);
	private static final GlobalTimer instance = new GlobalTimer();
	private final List<TimeoutSubmitter> emittedSubmitters = new ArrayList<>();

	private GlobalTimer() {
		super("Global TimeoutTimer", true);
	}

	public static GlobalTimer getInstance() {
		return instance;
	}

	public TimeoutSubmitter getSubmitter() {
		return new TimeoutSubmitter();
	}

	@Override
	public void cancel() {
		throw new UnsupportedOperationException("The TimeoutTimer must not be canceled manually!");
	}

	public boolean isTaskScheduled(final TimerTask task) {
		return this.getActiveTasks().contains(task);
	}

	public List<TimerTask> getActiveTasks() {
		try {
			Field outerQueueField = Timer.class.getDeclaredField("queue");
			outerQueueField.setAccessible(true);
			Object outerQueueObject = outerQueueField.get(this);
			Field innerQueueField = outerQueueObject.getClass().getDeclaredField("queue");
			Field innerScheduledFieldField = TimerTask.class.getClass().getDeclaredField("scheduled");
			innerQueueField.setAccessible(true);
			innerScheduledFieldField.setAccessible(true);
			TimerTask[] tasksAsArray = (TimerTask[])innerQueueField.get(outerQueueObject);
			List<TimerTask> tasks = new ArrayList<>();
			for (TimerTask task : tasksAsArray) {
				if (task != null && innerScheduledFieldField.getInt(task) >= 0) {
					tasks.add(task);
				}
			}
			return tasks;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			return new ArrayList<>();
		}
	}

	public int getNumberOfActiveTasks() {
		return this.getActiveTasks().size();
	}



	public class TimeoutSubmitter {
		private TimeoutSubmitter() {
			synchronized (instance) {
				GlobalTimer.this.emittedSubmitters.add(this);
			}
		}

		public synchronized TimerTask interruptMeAfterMS(final int delay, final String reason) {
			logger.info("Scheduling interrupt for thread {} in {}ms", Thread.currentThread(), delay);
			return this.interruptThreadAfterMS(Thread.currentThread(), delay, reason);
		}

		public synchronized TimerTask interruptMeAfterMS(final int delay, final String reason, final Runnable preInterruptionHook) {
			logger.info("Scheduling interrupt for thread {} in {}ms", Thread.currentThread(), delay);
			return this.interruptThreadAfterMS(Thread.currentThread(), delay, reason, preInterruptionHook);
		}

		public synchronized TimerTask interruptThreadAfterMS(final Thread thread, final long delay, final String reason) {
			return this.interruptThreadAfterMS(thread, delay, reason, null);
		}

		public synchronized TimerTask interruptThreadAfterMS(final Thread thread, final long delay, final String reason, final Runnable preInterruptionHook) {
			TimerTask task = new InterruptionTimerTask(reason, thread, preInterruptionHook);
			if (!GlobalTimer.this.emittedSubmitters.contains(this)) {
				throw new IllegalStateException("Cannot submit interrupt job to submitter " + this + " since it has already been closed!");
			}
			GlobalTimer.this.schedule(task, delay);

			/* create id for job and return it */
			logger.info("Job {} scheduled for in {}ms.", task, delay);
			return task;
		}

		public void close() {
			synchronized (instance) {
				GlobalTimer.this.emittedSubmitters.remove(this);
				logger.info("Canceled timer");
			}
		}
	}
}

package ai.libs.jaicore.concurrent;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import org.api4.java.common.control.ICancelable;

import ai.libs.jaicore.basic.sets.SetUtil;

public class TrackableTimer extends Timer implements ICancelable {

	private static final String MSG_ERROR = "TimerTasks are not trackable. Please create a TrackableTimerTask first and submit that one.";

	private final Queue<TrackableTimerTask> scheduledSingleInvocationTasks = new LinkedList<>();
	private final Queue<TrackableTimerTask> scheduledReocurringTasks = new LinkedList<>();
	private final Map<TimerTask, Long> ratesOfReocurringTasks = new HashMap<>();
	private boolean canceled;

	public TrackableTimer() {
		super();
	}

	public TrackableTimer(final boolean isDaemon) {
		super(isDaemon);
	}

	public TrackableTimer(final String name, final boolean isDaemon) {
		super(name, isDaemon);
	}

	public TrackableTimer(final String name) {
		super(name);
	}

	/**
	 * @deprecated({@link TrackableTimer} do not allow to schedule ordinary {@link TimerTask} objects but only {@link TrackableTimerTask} objects)
	 */
	@Deprecated
	@Override
	public void schedule(final TimerTask task, final Date time) {
		throw new UnsupportedOperationException(MSG_ERROR);
	}

	/**
	 * @deprecated({@link TrackableTimer} do not allow to schedule ordinary {@link TimerTask} objects but only {@link TrackableTimerTask} objects)
	 */
	@Override
	@Deprecated
	public void schedule(final TimerTask task, final Date time, final long period) {
		throw new UnsupportedOperationException(MSG_ERROR);
	}

	/**
	 * @deprecated({@link TrackableTimer} do not allow to schedule ordinary {@link TimerTask} objects but only {@link TrackableTimerTask} objects)
	 */
	@Override
	@Deprecated
	public void schedule(final TimerTask task, final long delay) {
		throw new UnsupportedOperationException(MSG_ERROR);
	}

	/**
	 * @deprecated({@link TrackableTimer} do not allow to schedule ordinary {@link TimerTask} objects but only {@link TrackableTimerTask} objects)
	 */
	@Override
	@Deprecated
	public void schedule(final TimerTask task, final long delay, final long period) {
		throw new UnsupportedOperationException(MSG_ERROR);
	}

	/**
	 * @deprecated({@link TrackableTimer} do not allow to schedule ordinary {@link TimerTask} objects but only {@link TrackableTimerTask} objects)
	 */
	@Override
	@Deprecated
	public void scheduleAtFixedRate(final TimerTask task, final Date firstTime, final long period) {
		throw new UnsupportedOperationException(MSG_ERROR);
	}

	/**
	 * @deprecated({@link TrackableTimer} do not allow to schedule ordinary {@link TimerTask} objects but only {@link TrackableTimerTask} objects)
	 */
	@Override
	@Deprecated
	public void scheduleAtFixedRate(final TimerTask task, final long delay, final long period) {
		throw new UnsupportedOperationException(MSG_ERROR);
	}

	public void schedule(final TrackableTimerTask task, final Date time) {
		super.schedule(task, time);
		synchronized (this.scheduledSingleInvocationTasks) {
			this.scheduledSingleInvocationTasks.add(task);
		}
	}

	public void schedule(final TrackableTimerTask task, final Date time, final long period) {
		super.schedule(task, time, period);
		synchronized (this.scheduledReocurringTasks) {
			this.scheduledReocurringTasks.add(task);
		}
		this.ratesOfReocurringTasks.put(task, period);
	}

	public void schedule(final TrackableTimerTask task, final long delay) {
		super.schedule(task, delay);
		synchronized (this.scheduledSingleInvocationTasks) {
			this.scheduledSingleInvocationTasks.add(task);
		}
	}

	public void schedule(final TrackableTimerTask task, final long delay, final long period) {
		super.schedule(task, delay, period);
		synchronized (this.scheduledReocurringTasks) {
			this.scheduledReocurringTasks.add(task);
		}
		this.ratesOfReocurringTasks.put(task, period);
	}

	public void scheduleAtFixedRate(final TrackableTimerTask task, final Date firstTime, final long period) {
		super.scheduleAtFixedRate(task, firstTime, period);
		synchronized (this.scheduledReocurringTasks) {
			this.scheduledReocurringTasks.add(task);
		}
		this.ratesOfReocurringTasks.put(task, period);
	}

	public void scheduleAtFixedRate(final TrackableTimerTask task, final long delay, final long period) {
		super.scheduleAtFixedRate(task, delay, period);
		synchronized (this.scheduledReocurringTasks) {
			this.scheduledReocurringTasks.add(task);
		}
		this.ratesOfReocurringTasks.put(task, period);
	}

	public boolean hasTaskBeenExecutedInPast(final TrackableTimerTask task) {
		return task.hasBeenExecuted();
	}

	public boolean willTaskBeExecutedInFuture(final TrackableTimerTask task) {
		if (this.canceled || task.isCanceled()) {
			return false;
		}
		if (this.scheduledSingleInvocationTasks.contains(task)) {
			return !this.hasTaskBeenExecutedInPast(task);
		}
		if (this.scheduledReocurringTasks.contains(task)) {
			return true;
		}

		/* if we come here, the TimerTask has not been scheduled in this timer */
		return false;
	}

	@Override
	public void cancel() {
		this.canceled = true;
		super.cancel();
	}

	public boolean isCanceld() {
		return this.canceled;
	}

	public List<TrackableTimerTask> getActiveTasks() {
		synchronized (this.scheduledSingleInvocationTasks) {
			synchronized (this.scheduledReocurringTasks) {
				return SetUtil.union(this.scheduledSingleInvocationTasks, this.scheduledReocurringTasks).stream().filter(this::willTaskBeExecutedInFuture).collect(Collectors.toList());
			}
		}
	}

	public int getNumberOfActiveTasks() {
		return this.getActiveTasks().size();
	}

	public boolean hasOpenTasks() {
		return !this.getActiveTasks().isEmpty();
	}
}

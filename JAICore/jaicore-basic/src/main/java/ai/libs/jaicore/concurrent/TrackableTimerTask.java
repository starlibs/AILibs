package ai.libs.jaicore.concurrent;

import java.util.TimerTask;

public abstract class TrackableTimerTask extends TimerTask {

	public static TrackableTimerTask get(final TimerTask tt) {
		return new WrappingTrackableTimerTask(tt);
	}

	private boolean canceled;
	private long lastExecution = -1;
	private boolean finished;

	@Override
	public final void run() {
		this.lastExecution = System.currentTimeMillis();
		this.exec();
		this.finished = true;
	}

	public abstract void exec();

	@Override
	public boolean cancel() {
		this.canceled = true;
		return super.cancel();
	}

	public boolean isCanceled() {
		return this.canceled;
	}

	public long getLastExecution() {
		return this.lastExecution;
	}

	public boolean hasBeenExecuted() {
		return this.lastExecution >= 0;
	}

	public boolean isFinished() {
		return this.finished;
	}
}

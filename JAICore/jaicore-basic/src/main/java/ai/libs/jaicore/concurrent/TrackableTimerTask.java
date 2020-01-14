package ai.libs.jaicore.concurrent;

import java.util.TimerTask;

public abstract class TrackableTimerTask extends TimerTask {

	public static TrackableTimerTask get(final TimerTask tt) {
		return new WrappingTrackableTimerTask(tt);
	}

	private boolean canceled;

	@Override
	public boolean cancel() {
		this.canceled = true;
		return super.cancel();
	}

	public boolean isCanceled() {
		return this.canceled;
	}
}

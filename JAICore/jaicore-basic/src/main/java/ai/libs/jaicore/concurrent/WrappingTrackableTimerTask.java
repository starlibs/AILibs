package ai.libs.jaicore.concurrent;

import java.util.TimerTask;

public class WrappingTrackableTimerTask extends TrackableTimerTask {

	private final TimerTask tt;

	public WrappingTrackableTimerTask(final TimerTask tt) {
		super();
		this.tt = tt;
	}

	@Override
	public void exec() {
		this.tt.run();
	}
}

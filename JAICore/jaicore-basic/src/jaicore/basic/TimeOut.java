package jaicore.basic;

import java.util.concurrent.TimeUnit;

public class TimeOut {

	private TimeUnit unit;
	private long duration;

	public TimeOut(final long duration, final TimeUnit unit) {
		this.duration = duration;
		this.unit = unit;
	}

	public long nanoseconds() {
		return TimeUnit.NANOSECONDS.convert(this.duration, this.unit);
	}

	public long milliseconds() {
		return TimeUnit.MILLISECONDS.convert(this.duration, this.unit);
	}

	public long minutes() {
		return TimeUnit.MINUTES.convert(this.duration, this.unit);
	}

	public long seconds() {
		return TimeUnit.SECONDS.convert(this.duration, this.unit);
	}

	public long hours() {
		return TimeUnit.HOURS.convert(this.duration, this.unit);
	}

	public long days() {
		return TimeUnit.DAYS.convert(this.duration, this.unit);
	}

}

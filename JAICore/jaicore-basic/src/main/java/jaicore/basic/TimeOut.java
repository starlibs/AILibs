package jaicore.basic;

import java.util.concurrent.TimeUnit;

public class TimeOut {

	private final TimeUnit unit;
	private final long duration;

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

	@Override
	public String toString() {
		return this.milliseconds() + "ms";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (this.duration ^ (this.duration >>> 32));
		result = prime * result + ((this.unit == null) ? 0 : this.unit.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		TimeOut other = (TimeOut) obj;
		if (this.duration != other.duration) {
			return false;
		}
		return this.unit == other.unit;
	}
}

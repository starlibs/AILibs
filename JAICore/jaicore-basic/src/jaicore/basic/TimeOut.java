package jaicore.basic;

import java.util.concurrent.TimeUnit;

public class TimeOut {

	private final TimeUnit timeUnit;
	private final long value;

	/**
	 * Standard c'tor to define a timeout together with its unit.
	 *
	 * @param value
	 *            The quantity of the timeout in terms of the given timeunit.
	 * @param timeUnit
	 *            The time unit determining how to interpret the value.
	 */
	public TimeOut(final long value, final TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
		this.value = value;
	}

	/**
	 * @return Returns the value of the timeout as seconds.
	 */
	public long seconds() {
		return TimeUnit.SECONDS.convert(this.value, this.timeUnit);
	}

	/**
	 * @return Returns the value of the timeout as milliseconds.
	 */
	public long milliseconds() {
		return TimeUnit.MILLISECONDS.convert(this.value, this.timeUnit);
	}

	/**
	 * @return Returns the value of the timeout as microseconds.
	 */
	public long microseconds() {
		return TimeUnit.MICROSECONDS.convert(this.value, this.timeUnit);
	}

	/**
	 * @return Returns the value of the timeout as hours.
	 */
	public long hours() {
		return TimeUnit.HOURS.convert(this.value, this.timeUnit);
	}

	/**
	 * @return Returns the value of the timeout as minutes.
	 */
	public long minutes() {
		return TimeUnit.MINUTES.convert(this.value, this.timeUnit);
	}

}

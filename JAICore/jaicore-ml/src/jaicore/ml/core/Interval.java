package jaicore.ml.core;

/**
 * Simple interval implementation, this can be used to query intervals in the {@link AbstractIntervalTree}.
 * @author mirkoj
 *
 */
public class Interval {
	private int lowerBound;

	private int upperBound;

	public Interval(int lowerBound, int upperBound) {
		super();
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public int getLowerBound() {
		return lowerBound;
	}

	public int getUpperBound() {
		return upperBound;
	}
}

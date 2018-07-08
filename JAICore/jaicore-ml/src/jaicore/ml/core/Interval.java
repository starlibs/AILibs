package jaicore.ml.core;

/**
 * Simple interval implementation, this can be used to query intervals in the {@link AbstractIntervalTree}.
 * @author mirkoj
 *
 */
public class Interval {
	private double lowerBound;

	private double upperBound;

	public Interval(double lowerBound, double upperBound) {
		super();
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public double getLowerBound() {
		return lowerBound;
	}

	public double getUpperBound() {
		return upperBound;
	}
}

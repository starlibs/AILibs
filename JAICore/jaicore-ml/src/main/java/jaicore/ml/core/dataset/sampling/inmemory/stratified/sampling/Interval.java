package jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling;

public class Interval {

	private double lowerBound;

	private double upperBound;

	public Interval(final double lowerBound, final double upperBound) {
		super();
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public double getLowerBound() {
		return this.lowerBound;
	}

	public void setLowerBound(final double lowerBound) {
		this.lowerBound = lowerBound;
	}

	public double getUpperBound() {
		return this.upperBound;
	}

	public void setUpperBound(final double upperBound) {
		this.upperBound = upperBound;
	}

	public boolean contains(final double d) {
		return (d >= this.lowerBound && d <= this.upperBound);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(this.lowerBound);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.upperBound);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		Interval other = (Interval) obj;
		if (Double.doubleToLongBits(this.lowerBound) != Double.doubleToLongBits(other.lowerBound)) {
			return false;
		}
		return Double.doubleToLongBits(this.upperBound) == Double.doubleToLongBits(other.upperBound);
	}

	@Override
	public String toString() {
		return "[" + this.lowerBound + ";" + this.upperBound + "]";
	}

}

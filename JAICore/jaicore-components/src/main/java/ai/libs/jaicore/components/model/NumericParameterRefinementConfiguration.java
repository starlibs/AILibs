package ai.libs.jaicore.components.model;

import ai.libs.jaicore.components.api.INumericParameterRefinementConfiguration;

public class NumericParameterRefinementConfiguration implements INumericParameterRefinementConfiguration {
	private final boolean initRefinementOnLogScale;
	private final double focusPoint;
	private final double logBasis;
	private final boolean initWithExtremalPoints; // make the end-points of the interval explicit choices on the first level
	private final int refinementsPerStep;
	private final double intervalLength;

	public NumericParameterRefinementConfiguration(final boolean initWithExtremalPoints, final int refinementsPerStep, final double intervalLength) {
		this(Double.NaN, 0, initWithExtremalPoints, refinementsPerStep, intervalLength);
	}

	public NumericParameterRefinementConfiguration(final double focusPoint, final double logBasis, final boolean initWithExtremalPoints, final int refinementsPerStep, final double intervalLength) {
		super();
		if (intervalLength <= 0) {
			throw new IllegalArgumentException("Interval length must be strictly positive but is " + intervalLength);
		}
		this.focusPoint = focusPoint;
		this.logBasis = logBasis;
		this.initRefinementOnLogScale = !Double.isNaN(focusPoint);
		this.initWithExtremalPoints = initWithExtremalPoints;
		this.refinementsPerStep = refinementsPerStep;
		this.intervalLength = intervalLength;
	}

	@Override
	public boolean isInitRefinementOnLogScale() {
		return this.initRefinementOnLogScale;
	}

	@Override
	public double getFocusPoint() {
		return this.focusPoint;
	}

	@Override
	public double getLogBasis() {
		return this.logBasis;
	}

	@Override
	public boolean isInitWithExtremalPoints() {
		return this.initWithExtremalPoints;
	}

	@Override
	public int getRefinementsPerStep() {
		return this.refinementsPerStep;
	}

	@Override
	public double getIntervalLength() {
		return this.intervalLength;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("[InitiallyLogScale:");
		sb.append(this.initRefinementOnLogScale);
		sb.append(",RefinementsPerStep:");
		sb.append(this.refinementsPerStep);
		sb.append(",intervalLength:");
		sb.append(this.intervalLength);
		sb.append("]");

		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(this.focusPoint);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (this.initRefinementOnLogScale ? 1231 : 1237);
		result = prime * result + (this.initWithExtremalPoints ? 1231 : 1237);
		temp = Double.doubleToLongBits(this.intervalLength);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.logBasis);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + this.refinementsPerStep;
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
		NumericParameterRefinementConfiguration other = (NumericParameterRefinementConfiguration) obj;
		if (Double.doubleToLongBits(this.focusPoint) != Double.doubleToLongBits(other.focusPoint)) {
			return false;
		}
		if (this.initRefinementOnLogScale != other.initRefinementOnLogScale) {
			return false;
		}
		if (this.initWithExtremalPoints != other.initWithExtremalPoints) {
			return false;
		}
		if (Double.doubleToLongBits(this.intervalLength) != Double.doubleToLongBits(other.intervalLength)) {
			return false;
		}
		if (Double.doubleToLongBits(this.logBasis) != Double.doubleToLongBits(other.logBasis)) {
			return false;
		}
		return this.refinementsPerStep == other.refinementsPerStep;
	}
}

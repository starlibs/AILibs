package hasco.model;

public class ParameterRefinementConfiguration {
	private final boolean initRefinementOnLogScale;
	private final double focusPoint;
	private final double logBasis;
	private final boolean initWithExtremalPoints; // make the end-points of the interval explicit choices on the first level
	private final int refinementsPerStep;
	private final double intervalLength;

	public ParameterRefinementConfiguration(final boolean initWithExtremalPoints, final int refinementsPerStep, final double intervalLength) {
		this(Double.NaN, 0, initWithExtremalPoints, refinementsPerStep, intervalLength);
	}

	public ParameterRefinementConfiguration(final double focusPoint, final double logBasis, final boolean initWithExtremalPoints, final int refinementsPerStep, final double intervalLength) {
		super();
		this.focusPoint = focusPoint;
		this.logBasis = logBasis;
		this.initRefinementOnLogScale = !Double.isNaN(focusPoint);
		this.initWithExtremalPoints = initWithExtremalPoints;
		this.refinementsPerStep = refinementsPerStep;
		this.intervalLength = intervalLength;
	}

	public boolean isInitRefinementOnLogScale() {
		return initRefinementOnLogScale;
	}

	public double getFocusPoint() {
		return focusPoint;
	}

	public double getLogBasis() {
		return logBasis;
	}

	public boolean isInitWithExtremalPoints() {
		return initWithExtremalPoints;
	}

	public int getRefinementsPerStep() {
		return refinementsPerStep;
	}

	public double getIntervalLength() {
		return intervalLength;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("[InitiallyLogScale:");
		sb.append(initRefinementOnLogScale);
		sb.append(",RefinementsPerStep:");
		sb.append(refinementsPerStep);
		sb.append(",intervalLength:");
		sb.append(intervalLength);
		sb.append("]");

		return sb.toString();
	}
}

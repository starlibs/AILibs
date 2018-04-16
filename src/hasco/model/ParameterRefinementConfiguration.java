package hasco.model;

public class ParameterRefinementConfiguration {
	private final boolean initRefinementOnLogScale = true;
	private final int refinementsPerStep;
	private final double intervalLength;

	public ParameterRefinementConfiguration(int refinementsPerStep, double intervalLength) {
		super();
		this.refinementsPerStep = refinementsPerStep;
		this.intervalLength = intervalLength;
	}

	public boolean isInitRefinementOnLogScale() {
		return initRefinementOnLogScale;
	}

	public int getRefinementsPerStep() {
		return refinementsPerStep;
	}

	public double getIntervalLength() {
		return intervalLength;
	}
}

package jaicore.search.algorithms.standard.uncertainty;

public class OversearchAvoidanceConfig<N> {
	
	public enum OversearchAvoidanceMode {
		PARETO_FRONT_SELECTION,
		TWO_PHASE_SELECTION,
		NONE
	}
	
	private OversearchAvoidanceMode oversearchAvoidanceMode;
	private boolean adjustPhaseLengthsDynamically = false;
	private long timeout;
	private int interval = 20;
	private int randomSampleAmount = 3;
	private double exploitationScoreThreshold = 0.05d;
	private double explorationUncertaintyThreshold = 0.05d;
	private ISolutionDistanceMetric<N> solutionDistanceMetric= (s1, s2) -> 0.0d;

	public OversearchAvoidanceConfig(OversearchAvoidanceMode mode) {
		this.oversearchAvoidanceMode = mode;
	}

	public OversearchAvoidanceMode getOversearchAvoidanceMode() {
		return oversearchAvoidanceMode;
	}

	public ISolutionDistanceMetric<N> getSolutionDistanceMetric() {
		return solutionDistanceMetric;
	}

	public OversearchAvoidanceConfig<N> setSolutionDistanceMetric(ISolutionDistanceMetric<N> solutionDistanceMetric) {
		this.solutionDistanceMetric = solutionDistanceMetric;
		return this;
	}

	public boolean getAdjustPhaseLengthsDynamically() {
		return adjustPhaseLengthsDynamically;
	}

	public void activateDynamicPhaseLengthsAdjustment(long timeout) {
		this.adjustPhaseLengthsDynamically = true;
		this.timeout = timeout;
	}
	
	public long getTimeout() {
		return this.timeout;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public int getRandomSampleAmount() {
		return randomSampleAmount;
	}

	public void setRandomSampleAmount(int randomSampleAmount) {
		this.randomSampleAmount = randomSampleAmount;
	}

	public double getExploitationScoreThreshold() {
		return exploitationScoreThreshold;
	}

	public void setExploitationScoreThreshold(double exploitationScoreThreshold) {
		this.exploitationScoreThreshold = exploitationScoreThreshold;
	}

	public double getExplorationUncertaintyThreshold() {
		return explorationUncertaintyThreshold;
	}

	public void setExplorationUncertaintyThreshold(double explorationUncertaintyThreshold) {
		this.explorationUncertaintyThreshold = explorationUncertaintyThreshold;
	}

}

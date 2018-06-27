package jaicore.search.algorithms.standard.uncertainty;

import jaicore.search.algorithms.interfaces.ISolutionEvaluator;

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
	private ISolutionDistanceMetric<N> solutionDistanceMetric;
	private ISolutionEvaluator<N, Double> solutionEvaluator;

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

	public ISolutionEvaluator<N, Double> getSolutionEvaluator() {
		return solutionEvaluator;
	}

	public OversearchAvoidanceConfig<N> setSolutionEvaluator(ISolutionEvaluator<N, Double> solutionEvaluator) {
		this.solutionEvaluator = solutionEvaluator;
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

}

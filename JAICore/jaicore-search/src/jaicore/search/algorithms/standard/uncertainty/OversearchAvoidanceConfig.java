package jaicore.search.algorithms.standard.uncertainty;

import jaicore.search.algorithms.standard.uncertainty.paretosearch.FirstInFirstOutComparator;
import jaicore.search.algorithms.standard.uncertainty.paretosearch.ParetoNode;

import java.util.Comparator;

public class OversearchAvoidanceConfig<N, V extends Comparable<V>> {
	
	public enum OversearchAvoidanceMode {
		PARETO_FRONT_SELECTION,
		TWO_PHASE_SELECTION,
		NONE
	}
	
	private OversearchAvoidanceMode oversearchAvoidanceMode;
	private long seed;
	private boolean adjustPhaseLengthsDynamically = false;
	private long timeout;
	private int interval = 50;
	private double exploitationScoreThreshold = 0.1d;
	private double explorationUncertaintyThreshold = 0.1d;
	private double minimumSolutionDistanceForExploration = 0.0d;
	private ISolutionDistanceMetric<N> solutionDistanceMetric= (s1, s2) -> 1.0d;
	private Comparator<ParetoNode<N, V>> paretoComparator = new FirstInFirstOutComparator<>();

	public OversearchAvoidanceConfig(OversearchAvoidanceMode mode, long seed) {
		this.oversearchAvoidanceMode = mode;
		this.seed = seed;
	}

	public OversearchAvoidanceMode getOversearchAvoidanceMode() {
		return oversearchAvoidanceMode;
	}

	public ISolutionDistanceMetric<N> getSolutionDistanceMetric() {
		return solutionDistanceMetric;
	}

	public OversearchAvoidanceConfig<N, V> setSolutionDistanceMetric(ISolutionDistanceMetric<N> solutionDistanceMetric) {
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

	public double getMinimumSolutionDistanceForExploration() {
		return minimumSolutionDistanceForExploration;
	}

	public void setMinimumSolutionDistanceForExploration(double minimumSolutionDistanceForExploration) {
		this.minimumSolutionDistanceForExploration = minimumSolutionDistanceForExploration;
	}
	
	public long getSeed() {
		return this.seed;
	}

	public void setParetoComperator(Comparator<ParetoNode<N, V>> paretoComparator) {
		this.paretoComparator = paretoComparator;
	}
	
	public Comparator<ParetoNode<N, V>> getParetoComperator() {
		return this.paretoComparator;
	}

}
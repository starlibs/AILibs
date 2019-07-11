package ai.libs.jaicore.search.algorithms.standard.uncertainty;

import java.util.Comparator;

import ai.libs.jaicore.search.algorithms.standard.uncertainty.paretosearch.FirstInFirstOutComparator;
import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

public class OversearchAvoidanceConfig<N, A, V extends Comparable<V>> {

	public enum OversearchAvoidanceMode {
		PARETO_FRONT_SELECTION, TWO_PHASE_SELECTION, NONE
	}

	private OversearchAvoidanceMode oversearchAvoidanceMode;
	private long seed;
	private boolean adjustPhaseLengthsDynamically = false;
	private long timeout;
	private int interval = 50;
	private double exploitationScoreThreshold = 0.1d;
	private double explorationUncertaintyThreshold = 0.1d;
	private double minimumSolutionDistanceForExploration = 0.0d;
	private ISolutionDistanceMetric<N> solutionDistanceMetric = (s1, s2) -> 1.0d;
	private Comparator<BackPointerPath<N, A, V>> paretoComparator = new FirstInFirstOutComparator<>();

	public OversearchAvoidanceConfig(final OversearchAvoidanceMode mode, final long seed) {
		this.oversearchAvoidanceMode = mode;
		this.seed = seed;
	}

	public OversearchAvoidanceMode getOversearchAvoidanceMode() {
		return this.oversearchAvoidanceMode;
	}

	public ISolutionDistanceMetric<N> getSolutionDistanceMetric() {
		return this.solutionDistanceMetric;
	}

	public OversearchAvoidanceConfig<N, A, V> setSolutionDistanceMetric(final ISolutionDistanceMetric<N> solutionDistanceMetric) {
		this.solutionDistanceMetric = solutionDistanceMetric;
		return this;
	}

	public boolean getAdjustPhaseLengthsDynamically() {
		return this.adjustPhaseLengthsDynamically;
	}

	public void activateDynamicPhaseLengthsAdjustment(final long timeout) {
		this.adjustPhaseLengthsDynamically = true;
		this.timeout = timeout;
	}

	public long getTimeout() {
		return this.timeout;
	}

	public int getInterval() {
		return this.interval;
	}

	public void setInterval(final int interval) {
		this.interval = interval;
	}

	public double getExploitationScoreThreshold() {
		return this.exploitationScoreThreshold;
	}

	public void setExploitationScoreThreshold(final double exploitationScoreThreshold) {
		this.exploitationScoreThreshold = exploitationScoreThreshold;
	}

	public double getExplorationUncertaintyThreshold() {
		return this.explorationUncertaintyThreshold;
	}

	public void setExplorationUncertaintyThreshold(final double explorationUncertaintyThreshold) {
		this.explorationUncertaintyThreshold = explorationUncertaintyThreshold;
	}

	public double getMinimumSolutionDistanceForExploration() {
		return this.minimumSolutionDistanceForExploration;
	}

	public void setMinimumSolutionDistanceForExploration(final double minimumSolutionDistanceForExploration) {
		this.minimumSolutionDistanceForExploration = minimumSolutionDistanceForExploration;
	}

	public long getSeed() {
		return this.seed;
	}

	public void setParetoComparator(final Comparator<BackPointerPath<N, A, V>> paretoComparator) {
		this.paretoComparator = paretoComparator;
	}

	public Comparator<BackPointerPath<N, A, V>> getParetoComperator() {
		return this.paretoComparator;
	}

}
package hasco.variants.forwarddecomposition.twophase;

import hasco.core.HASCOSolutionCandidate;

public class TwoPhaseHASCOReport {
	private final int numSolutionsInPhase1;
	private final int durationPhase1;
	private final HASCOSolutionCandidate<Double> returnedSolution;

	public TwoPhaseHASCOReport(int numSolutionsInPhase1, int durationPhase1, HASCOSolutionCandidate<Double> returnedSolution) {
		super();
		this.numSolutionsInPhase1 = numSolutionsInPhase1;
		this.durationPhase1 = durationPhase1;
		this.returnedSolution = returnedSolution;
	}

	public int getNumSolutionsInPhase1() {
		return numSolutionsInPhase1;
	}

	public int getDurationPhase1() {
		return durationPhase1;
	}

	public HASCOSolutionCandidate<Double> getReturnedSolution() {
		return returnedSolution;
	}
}

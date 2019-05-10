package hasco.core;

import java.util.List;

public class HASCORunReport<V extends Comparable<V>> {
	private final List<HASCOSolutionCandidate<V>> solutionCandidates;

	public HASCORunReport(List<HASCOSolutionCandidate<V>> solutionCandidates) {
		super();
		this.solutionCandidates = solutionCandidates;
	}

	public List<HASCOSolutionCandidate<V>> getSolutionCandidates() {
		return solutionCandidates;
	}
}

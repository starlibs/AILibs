package hasco.events;

import hasco.core.HASCOSolutionCandidate;
import jaicore.basic.algorithm.events.SolutionCandidateFoundEvent;

public class HASCOSolutionEvent<V extends Comparable<V>> extends SolutionCandidateFoundEvent<HASCOSolutionCandidate<V>> {

	public HASCOSolutionEvent(String algorithmId, HASCOSolutionCandidate<V> solutionCandidate) {
		super(algorithmId, solutionCandidate);
	}
}

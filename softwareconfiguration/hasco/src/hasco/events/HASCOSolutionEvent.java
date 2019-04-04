package hasco.events;

import hasco.core.HASCOSolutionCandidate;
import jaicore.basic.algorithm.SolutionCandidateFoundEvent;

public class HASCOSolutionEvent<V extends Comparable<V>> extends SolutionCandidateFoundEvent<HASCOSolutionCandidate<V>> {

	public HASCOSolutionEvent(HASCOSolutionCandidate<V> solutionCandidate) {
		super(solutionCandidate);
	}
}

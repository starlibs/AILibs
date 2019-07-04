package ai.libs.hasco.events;

import ai.libs.hasco.core.HASCOSolutionCandidate;
import ai.libs.jaicore.basic.algorithm.events.ASolutionCandidateFoundEvent;
import ai.libs.jaicore.basic.algorithm.events.ScoredSolutionCandidateFoundEvent;

public class HASCOSolutionEvent<V extends Comparable<V>> extends ASolutionCandidateFoundEvent<HASCOSolutionCandidate<V>> implements ScoredSolutionCandidateFoundEvent<HASCOSolutionCandidate<V>, V> {

	public HASCOSolutionEvent(String algorithmId, HASCOSolutionCandidate<V> solutionCandidate) {
		super(algorithmId, solutionCandidate);
	}

	@Override
	public V getScore() {
		return getSolutionCandidate().getScore();
	}
}

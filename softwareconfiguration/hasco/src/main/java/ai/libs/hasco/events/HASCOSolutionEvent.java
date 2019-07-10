package ai.libs.hasco.events;

import org.api4.java.algorithm.events.ASolutionCandidateFoundEvent;
import org.api4.java.algorithm.events.ScoredSolutionCandidateFoundEvent;

import ai.libs.hasco.core.HASCOSolutionCandidate;

public class HASCOSolutionEvent<V extends Comparable<V>> extends ASolutionCandidateFoundEvent<HASCOSolutionCandidate<V>> implements ScoredSolutionCandidateFoundEvent<HASCOSolutionCandidate<V>, V> {

	public HASCOSolutionEvent(final String algorithmId, final HASCOSolutionCandidate<V> solutionCandidate) {
		super(algorithmId, solutionCandidate);
	}

	@Override
	public V getScore() {
		return this.getSolutionCandidate().getScore();
	}
}

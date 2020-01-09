package ai.libs.hasco.events;

import org.api4.java.algorithm.events.result.IScoredSolutionCandidateFoundEvent;

import ai.libs.hasco.core.HASCOSolutionCandidate;
import ai.libs.jaicore.basic.algorithm.ASolutionCandidateFoundEvent;

public class HASCOSolutionEvent<V extends Comparable<V>> extends ASolutionCandidateFoundEvent<HASCOSolutionCandidate<V>> implements IScoredSolutionCandidateFoundEvent<HASCOSolutionCandidate<V>, V> {

	public HASCOSolutionEvent(final String algorithmId, final HASCOSolutionCandidate<V> solutionCandidate) {
		super(algorithmId, solutionCandidate);
	}

	@Override
	public V getScore() {
		return this.getSolutionCandidate().getScore();
	}
}

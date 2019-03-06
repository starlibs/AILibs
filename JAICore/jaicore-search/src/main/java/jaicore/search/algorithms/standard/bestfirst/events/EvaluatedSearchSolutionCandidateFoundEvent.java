package jaicore.search.algorithms.standard.bestfirst.events;

import jaicore.basic.algorithm.events.ScoredSolutionCandidateFoundEvent;
import jaicore.search.model.other.EvaluatedSearchGraphPath;

public class EvaluatedSearchSolutionCandidateFoundEvent<N, A, V extends Comparable<V>> extends GraphSearchSolutionCandidateFoundEvent<N, A, EvaluatedSearchGraphPath<N, A, V>> implements ScoredSolutionCandidateFoundEvent<EvaluatedSearchGraphPath<N, A, V>, V> {

	public EvaluatedSearchSolutionCandidateFoundEvent(final String algorithmId, final EvaluatedSearchGraphPath<N, A, V> solutionCandidate) {
		super(algorithmId, solutionCandidate);
	}

	@Override
	public V getScore() {
		return this.getSolutionCandidate().getScore();
	}
}

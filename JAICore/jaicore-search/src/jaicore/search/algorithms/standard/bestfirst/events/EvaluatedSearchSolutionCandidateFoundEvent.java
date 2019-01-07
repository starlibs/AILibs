package jaicore.search.algorithms.standard.bestfirst.events;

import jaicore.search.model.other.EvaluatedSearchGraphPath;

public class EvaluatedSearchSolutionCandidateFoundEvent<N, A, V extends Comparable<V>> extends GraphSearchSolutionCandidateFoundEvent<N, A, EvaluatedSearchGraphPath<N, A, V>> {

	public EvaluatedSearchSolutionCandidateFoundEvent(EvaluatedSearchGraphPath<N, A, V> solutionCandidate) {
		super(solutionCandidate);
	}

	public EvaluatedSearchGraphPath<N, A, V> getSolutionCandidate() {
		return (EvaluatedSearchGraphPath<N, A, V>) super.getSolutionCandidate();
	}
}

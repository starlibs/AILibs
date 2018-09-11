package jaicore.search.algorithms.standard.bestfirst.events;

import jaicore.search.model.other.EvaluatedSearchGraphPath;

public class EvaluatedSearchSolutionCandidateFoundEvent<T, A, V extends Comparable<V>> extends GraphSearchSolutionCandidateFoundEvent<T, A> {

	public EvaluatedSearchSolutionCandidateFoundEvent(EvaluatedSearchGraphPath<T, A, V> solutionCandidate) {
		super(solutionCandidate);
	}

	@SuppressWarnings("unchecked")
	public EvaluatedSearchGraphPath<T, A, V> getSolutionCandidate() {
		return (EvaluatedSearchGraphPath<T, A, V>) super.getSolutionCandidate();
	}
}

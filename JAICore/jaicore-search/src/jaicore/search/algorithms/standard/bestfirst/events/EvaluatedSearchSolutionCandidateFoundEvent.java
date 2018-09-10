package jaicore.search.algorithms.standard.bestfirst.events;

import jaicore.basic.algorithm.SolutionCandidateFoundEvent;
import jaicore.search.model.other.EvaluatedSearchGraphPath;

public class EvaluatedSearchSolutionCandidateFoundEvent<T,A,V extends Comparable<V>> extends SolutionCandidateFoundEvent<EvaluatedSearchGraphPath<T, A, V>> {

	public EvaluatedSearchSolutionCandidateFoundEvent(EvaluatedSearchGraphPath<T, A, V> solutionCandidate) {
		super(solutionCandidate);
	}
}

package jaicore.search.algorithms.standard.bestfirst.events;

import jaicore.basic.algorithm.SolutionCandidateFoundEvent;
import jaicore.search.model.other.EvaluatedSearchGraphPath;

public class GraphSearchSolutionCandidateFoundEvent<T, A, V extends Comparable<V>> extends SolutionCandidateFoundEvent<EvaluatedSearchGraphPath<T, A, V>> {

	public GraphSearchSolutionCandidateFoundEvent(EvaluatedSearchGraphPath<T, A, V> solution) {
		super(solution);
	}
}

package jaicore.search.algorithms.standard.bestfirst.events;

import jaicore.basic.algorithm.SolutionCandidateFoundEvent;
import jaicore.search.model.other.SearchGraphPath;

public class GraphSearchSolutionCandidateFoundEvent<T, A> extends SolutionCandidateFoundEvent<SearchGraphPath<T, A>> {

	public GraphSearchSolutionCandidateFoundEvent(SearchGraphPath<T, A> solution) {
		super(solution);
	}
}

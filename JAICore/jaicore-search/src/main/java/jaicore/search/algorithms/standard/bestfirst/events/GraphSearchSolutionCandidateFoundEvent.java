package jaicore.search.algorithms.standard.bestfirst.events;

import jaicore.basic.algorithm.events.ASolutionCandidateFoundEvent;
import jaicore.search.model.other.SearchGraphPath;

/**
 * 
 * @author fmohr
 *
 * @param <N> the node class
 * @param <A> the arc class
 * @param <S> the solution coding class
 */
public class GraphSearchSolutionCandidateFoundEvent<N, A, S extends SearchGraphPath<N, A>> extends ASolutionCandidateFoundEvent<S> {

	public GraphSearchSolutionCandidateFoundEvent(String algorithmId, S solution) {
		super(algorithmId, solution);
	}
}

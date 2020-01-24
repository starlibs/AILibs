package ai.libs.jaicore.search.algorithms.standard.bestfirst.events;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.ASolutionCandidateFoundEvent;
import ai.libs.jaicore.search.model.other.SearchGraphPath;

/**
 *
 * @author fmohr
 *
 * @param <N> the node class
 * @param <A> the arc class
 * @param <S> the solution coding class
 */
public class GraphSearchSolutionCandidateFoundEvent<N, A, S extends SearchGraphPath<N, A>> extends ASolutionCandidateFoundEvent<S> {

	public GraphSearchSolutionCandidateFoundEvent(final IAlgorithm<?, ?> algorithm, final S solution) {
		super(algorithm, solution);
	}
}

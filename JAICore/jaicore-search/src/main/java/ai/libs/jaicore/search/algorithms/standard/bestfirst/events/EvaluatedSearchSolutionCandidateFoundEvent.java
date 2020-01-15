package ai.libs.jaicore.search.algorithms.standard.bestfirst.events;

import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.events.result.IScoredSolutionCandidateFoundEvent;

import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;

public class EvaluatedSearchSolutionCandidateFoundEvent<N, A, V extends Comparable<V>> extends GraphSearchSolutionCandidateFoundEvent<N, A, EvaluatedSearchGraphPath<N, A, V>> implements IScoredSolutionCandidateFoundEvent<EvaluatedSearchGraphPath<N, A, V>, V> {

	public EvaluatedSearchSolutionCandidateFoundEvent(final IAlgorithm<?, ?> algorithm, final EvaluatedSearchGraphPath<N, A, V> solutionCandidate) {
		super(algorithm, solutionCandidate);
	}

	@Override
	public V getScore() {
		return this.getSolutionCandidate().getScore();
	}
}

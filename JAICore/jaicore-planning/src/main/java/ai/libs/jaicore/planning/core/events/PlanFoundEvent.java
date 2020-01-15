package ai.libs.jaicore.planning.core.events;

import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.events.result.IScoredSolutionCandidateFoundEvent;

import ai.libs.jaicore.basic.algorithm.ASolutionCandidateFoundEvent;
import ai.libs.jaicore.planning.core.EvaluatedPlan;

public class PlanFoundEvent<V extends Comparable<V>> extends ASolutionCandidateFoundEvent<EvaluatedPlan<V>> implements IScoredSolutionCandidateFoundEvent<EvaluatedPlan<V>, V> {

	public PlanFoundEvent(final IAlgorithm<?, ?> algorithm, final EvaluatedPlan<V> solutionCandidate) {
		super(algorithm, solutionCandidate);
	}

	@Override
	public V getScore() {
		return this.getSolutionCandidate().getScore();
	}
}

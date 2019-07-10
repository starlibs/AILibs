package ai.libs.jaicore.planning.core.events;

import org.api4.java.algorithm.events.ASolutionCandidateFoundEvent;
import org.api4.java.algorithm.events.ScoredSolutionCandidateFoundEvent;

import ai.libs.jaicore.planning.core.Action;
import ai.libs.jaicore.planning.core.EvaluatedPlan;

public class PlanFoundEvent<A extends Action,V extends Comparable<V>> extends ASolutionCandidateFoundEvent<EvaluatedPlan<V>> implements ScoredSolutionCandidateFoundEvent<EvaluatedPlan<V>, V> {

	public PlanFoundEvent(final String algorithmId, final EvaluatedPlan<V> solutionCandidate) {
		super(algorithmId, solutionCandidate);
	}

	@Override
	public V getScore() {
		return this.getSolutionCandidate().getScore();
	}
}

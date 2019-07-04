package ai.libs.jaicore.planning.core.events;

import ai.libs.jaicore.basic.algorithm.events.ASolutionCandidateFoundEvent;
import ai.libs.jaicore.basic.algorithm.events.ScoredSolutionCandidateFoundEvent;
import ai.libs.jaicore.planning.core.Action;
import ai.libs.jaicore.planning.core.EvaluatedPlan;

public class PlanFoundEvent<A extends Action,V extends Comparable<V>> extends ASolutionCandidateFoundEvent<EvaluatedPlan<V>> implements ScoredSolutionCandidateFoundEvent<EvaluatedPlan<V>, V> {

	public PlanFoundEvent(String algorithmId, EvaluatedPlan<V> solutionCandidate) {
		super(algorithmId, solutionCandidate);
	}

	@Override
	public V getScore() {
		return getSolutionCandidate().getScore();
	}
}

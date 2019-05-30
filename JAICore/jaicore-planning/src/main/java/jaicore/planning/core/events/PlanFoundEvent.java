package jaicore.planning.core.events;

import jaicore.basic.algorithm.events.ASolutionCandidateFoundEvent;
import jaicore.basic.algorithm.events.ScoredSolutionCandidateFoundEvent;
import jaicore.planning.core.Action;
import jaicore.planning.core.EvaluatedPlan;

public class PlanFoundEvent<A extends Action,V extends Comparable<V>> extends ASolutionCandidateFoundEvent<EvaluatedPlan<V>> implements ScoredSolutionCandidateFoundEvent<EvaluatedPlan<V>, V> {

	public PlanFoundEvent(String algorithmId, EvaluatedPlan<V> solutionCandidate) {
		super(algorithmId, solutionCandidate);
	}

	@Override
	public V getScore() {
		return getSolutionCandidate().getScore();
	}
}

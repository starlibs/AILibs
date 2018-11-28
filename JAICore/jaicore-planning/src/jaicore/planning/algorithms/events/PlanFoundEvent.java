package jaicore.planning.algorithms.events;

import jaicore.basic.algorithm.SolutionCandidateFoundEvent;
import jaicore.planning.EvaluatedPlan;
import jaicore.planning.model.core.Action;

public class PlanFoundEvent<A extends Action,V extends Comparable<V>> extends SolutionCandidateFoundEvent<EvaluatedPlan<A, V>> {

	public PlanFoundEvent(EvaluatedPlan<A, V> solutionCandidate) {
		super(solutionCandidate);
	}
}

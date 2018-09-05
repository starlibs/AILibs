package jaicore.planning;

import java.util.List;

import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Plan;

public class EvaluatedPlan<A extends Action, V> extends Plan<A> {
	private final V score;

	public EvaluatedPlan(Plan<A> plan, V score) {
		this (plan.getActions(), score);
	}
	
	public EvaluatedPlan(List<A> plan, V score) {
		super(plan);
		this.score = score;
	}
	
	public V getScore() {
		return score;
	}
}

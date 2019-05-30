package jaicore.planning.core;

import java.util.List;

import jaicore.basic.ScoredItem;

public class EvaluatedPlan<V extends Comparable<V>> extends Plan implements ScoredItem<V> {
	private final V score;

	public EvaluatedPlan(Plan plan, V score) {
		this (plan.getActions(), score);
	}
	
	public EvaluatedPlan(List<Action> plan, V score) {
		super(plan);
		this.score = score;
	}
	
	public V getScore() {
		return score;
	}
}

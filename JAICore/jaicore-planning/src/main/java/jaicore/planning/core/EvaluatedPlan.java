package jaicore.planning.core;

import java.util.List;

import jaicore.basic.ScoredItem;

public class EvaluatedPlan<A extends Action, V extends Comparable<V>> extends Plan<A> implements ScoredItem<V> {
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

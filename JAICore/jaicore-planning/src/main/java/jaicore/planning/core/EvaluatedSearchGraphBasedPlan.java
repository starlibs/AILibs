package jaicore.planning.core;

import java.util.List;

import jaicore.search.model.other.SearchGraphPath;

public class EvaluatedSearchGraphBasedPlan<V extends Comparable<V>, N> extends EvaluatedPlan<V> {

	private final SearchGraphPath<N, ?> searchGraphPath;

	public EvaluatedSearchGraphBasedPlan(Plan plan, V score, SearchGraphPath<N, ?> searchGraphPath) {
		super(plan, score);
		this.searchGraphPath = searchGraphPath;
	}
	
	public EvaluatedSearchGraphBasedPlan(EvaluatedPlan<V> plan, SearchGraphPath<N, ?> searchGraphPath) {
		super(plan, plan.getScore());
		this.searchGraphPath = searchGraphPath;
	}
	
	public EvaluatedSearchGraphBasedPlan(List<Action> plan, V score, SearchGraphPath<N, ?> searchGraphPath) {
		super(plan, score);
		this.searchGraphPath = searchGraphPath;
	}

	public SearchGraphPath<N, ?> getPath() {
		return searchGraphPath;
	}
}

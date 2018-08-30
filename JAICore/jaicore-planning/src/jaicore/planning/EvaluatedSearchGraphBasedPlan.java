package jaicore.planning;

import java.util.List;

import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Plan;
import jaicore.search.model.other.SearchGraphPath;

public class EvaluatedSearchGraphBasedPlan<A extends Action,V extends Comparable<V>, N> extends EvaluatedPlan<A, V> {

	private final SearchGraphPath<N, ?> searchGraphPath;

	public EvaluatedSearchGraphBasedPlan(Plan<A> plan, V score, SearchGraphPath<N, ?> searchGraphPath) {
		super(plan, score);
		this.searchGraphPath = searchGraphPath;
	}
	
	public EvaluatedSearchGraphBasedPlan(EvaluatedPlan<A, V> plan, SearchGraphPath<N, ?> searchGraphPath) {
		super(plan, plan.getScore());
		this.searchGraphPath = searchGraphPath;
	}
	
	public EvaluatedSearchGraphBasedPlan(List<A> plan, V score, SearchGraphPath<N, ?> searchGraphPath) {
		super(plan, score);
		this.searchGraphPath = searchGraphPath;
	}

	public SearchGraphPath<N, ?> getPath() {
		return searchGraphPath;
	}
}

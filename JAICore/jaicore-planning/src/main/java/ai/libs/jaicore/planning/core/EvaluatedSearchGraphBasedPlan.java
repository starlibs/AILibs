package ai.libs.jaicore.planning.core;

import java.util.List;

import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;

public class EvaluatedSearchGraphBasedPlan<V extends Comparable<V>, N> extends EvaluatedPlan<V> {

	private final SearchGraphPath<N, ?> searchGraphPath;

	public EvaluatedSearchGraphBasedPlan(final Plan plan, final EvaluatedSearchGraphPath<N, ?, V> searchGraphPath) {
		this(plan, searchGraphPath.getScore(), searchGraphPath);
	}

	public EvaluatedSearchGraphBasedPlan(final Plan plan, final V score, final SearchGraphPath<N, ?> searchGraphPath) {
		super(plan, score);
		this.searchGraphPath = searchGraphPath;
	}

	public EvaluatedSearchGraphBasedPlan(final EvaluatedPlan<V> plan, final SearchGraphPath<N, ?> searchGraphPath) {
		super(plan, plan.getScore());
		this.searchGraphPath = searchGraphPath;
	}

	public EvaluatedSearchGraphBasedPlan(final List<Action> plan, final V score, final SearchGraphPath<N, ?> searchGraphPath) {
		super(plan, score);
		this.searchGraphPath = searchGraphPath;
	}

	public SearchGraphPath<N, ?> getPath() {
		return this.searchGraphPath;
	}
}

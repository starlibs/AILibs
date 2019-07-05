package ai.libs.jaicore.planning.core;

import java.util.List;

import ai.libs.jaicore.planning.core.interfaces.IEvaluatedGraphSearchBasedPlan;
import ai.libs.jaicore.planning.core.interfaces.IEvaluatedPlan;
import ai.libs.jaicore.planning.core.interfaces.IPlan;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;

public class EvaluatedSearchGraphBasedPlan<N, A, V extends Comparable<V>> extends EvaluatedPlan<V> implements IEvaluatedGraphSearchBasedPlan<N, A, V> {

	private final SearchGraphPath<N, A> searchGraphPath;

	public EvaluatedSearchGraphBasedPlan(final IPlan plan, final EvaluatedSearchGraphPath<N, A, V> searchGraphPath) {
		this(plan, searchGraphPath.getScore(), searchGraphPath);
	}

	public EvaluatedSearchGraphBasedPlan(final IPlan plan, final V score, final SearchGraphPath<N, A> searchGraphPath) {
		super(plan, score);
		this.searchGraphPath = searchGraphPath;
	}

	public EvaluatedSearchGraphBasedPlan(final IEvaluatedPlan<V> plan, final SearchGraphPath<N, A> searchGraphPath) {
		super(plan, plan.getScore());
		this.searchGraphPath = searchGraphPath;
	}

	public EvaluatedSearchGraphBasedPlan(final List<Action> plan, final V score, final SearchGraphPath<N, A> searchGraphPath) {
		super(plan, score);
		this.searchGraphPath = searchGraphPath;
	}

	@Override
	public SearchGraphPath<N, A> getSearchGraphPath() {
		return this.searchGraphPath;
	}
}

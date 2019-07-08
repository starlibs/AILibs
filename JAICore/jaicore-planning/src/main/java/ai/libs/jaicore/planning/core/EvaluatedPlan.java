package ai.libs.jaicore.planning.core;

import java.util.List;

import ai.libs.jaicore.planning.core.interfaces.IEvaluatedPlan;
import ai.libs.jaicore.planning.core.interfaces.IPlan;

public class EvaluatedPlan<V extends Comparable<V>> extends Plan implements IEvaluatedPlan<V> {
	private final V score;

	public EvaluatedPlan(final IPlan plan, final V score) {
		this (plan.getActions(), score);
	}

	public EvaluatedPlan(final List<Action> plan, final V score) {
		super(plan);
		this.score = score;
	}

	@Override
	public V getScore() {
		return this.score;
	}
}

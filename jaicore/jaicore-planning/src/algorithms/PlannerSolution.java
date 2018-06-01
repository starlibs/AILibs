package jaicore.planning.algorithms;

import java.util.List;

import jaicore.planning.model.core.Action;

public class PlannerSolution implements IPlanningSolution {

	private final List<Action> plan;

	public PlannerSolution(List<Action> plan) {
		super();
		this.plan = plan;
	}

	@Override
	public List<Action> getPlan() {
		return plan;
	}

}

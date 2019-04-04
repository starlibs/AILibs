package jaicore.planning.model;

import jaicore.basic.IObjectEvaluator;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.core.Plan;
import jaicore.planning.model.task.IHTNPlanningProblem;
import jaicore.planning.model.task.stn.Method;

public class CostSensitiveHTNPlanningProblem<O extends Operation, M extends Method, A extends Action, P extends IHTNPlanningProblem<O, M, A>, V extends Comparable<V>> {
	private final P corePlanningProblem;
	private final IObjectEvaluator<Plan<A>, V> planEvaluator;

	public CostSensitiveHTNPlanningProblem(P corePlanningProblem, IObjectEvaluator<Plan<A>, V> planEvaluator) {
		super();
		this.corePlanningProblem = corePlanningProblem;
		this.planEvaluator = planEvaluator;
	}

	public P getCorePlanningProblem() {
		return corePlanningProblem;
	}

	public IObjectEvaluator<Plan<A>, V> getPlanEvaluator() {
		return planEvaluator;
	}
}

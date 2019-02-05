package jaicore.planning.hierarchical.problems.htn;

import java.util.HashMap;
import java.util.Map;

import jaicore.basic.IObjectEvaluator;
import jaicore.logging.ToJSONStringUtil;
import jaicore.planning.core.Action;
import jaicore.planning.core.Plan;

public class CostSensitiveHTNPlanningProblem<A extends Action, V extends Comparable<V>> {
	private final IHTNPlanningProblem corePlanningProblem;
	private final IObjectEvaluator<Plan<A>, V> planEvaluator;

	public CostSensitiveHTNPlanningProblem(IHTNPlanningProblem corePlanningProblem, IObjectEvaluator<Plan<A>, V> planEvaluator) {
		super();
		this.corePlanningProblem = corePlanningProblem;
		this.planEvaluator = planEvaluator;
	}

	public IHTNPlanningProblem getCorePlanningProblem() {
		return corePlanningProblem;
	}

	public IObjectEvaluator<Plan<A>, V> getPlanEvaluator() {
		return planEvaluator;
	}
	
	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("corePlanningProblem", this.corePlanningProblem);
		fields.put("planEvaluator", this.planEvaluator);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
}

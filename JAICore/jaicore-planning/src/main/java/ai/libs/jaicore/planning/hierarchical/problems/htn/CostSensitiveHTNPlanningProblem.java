package ai.libs.jaicore.planning.hierarchical.problems.htn;

import java.util.HashMap;
import java.util.Map;

import ai.libs.jaicore.basic.IObjectEvaluator;
import ai.libs.jaicore.logging.ToJSONStringUtil;
import ai.libs.jaicore.planning.core.Plan;

public class CostSensitiveHTNPlanningProblem<IPlanning extends IHTNPlanningProblem, V extends Comparable<V>> {
	private final IPlanning corePlanningProblem;
	private final IObjectEvaluator<Plan, V> planEvaluator;

	public CostSensitiveHTNPlanningProblem(IPlanning corePlanningProblem, IObjectEvaluator<Plan, V> planEvaluator) {
		super();
		this.corePlanningProblem = corePlanningProblem;
		this.planEvaluator = planEvaluator;
	}

	public IPlanning getCorePlanningProblem() {
		return corePlanningProblem;
	}

	public IObjectEvaluator<Plan, V> getPlanEvaluator() {
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

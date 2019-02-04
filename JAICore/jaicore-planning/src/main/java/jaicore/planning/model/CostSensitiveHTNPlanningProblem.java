package jaicore.planning.model;

import java.util.HashMap;
import java.util.Map;

import jaicore.basic.IObjectEvaluator;
import jaicore.logging.ToJSONStringUtil;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.core.Plan;
import jaicore.planning.model.task.IHTNPlanningProblem;
import jaicore.planning.model.task.stn.Method;

public class CostSensitiveHTNPlanningProblem<O extends Operation, M extends Method, A extends Action, P extends IHTNPlanningProblem<O, M, A>, V extends Comparable<V>> {
	private final P corePlanningProblem;
	private final IObjectEvaluator<Plan<A>, V> planEvaluator;

	public CostSensitiveHTNPlanningProblem(final P corePlanningProblem, final IObjectEvaluator<Plan<A>, V> planEvaluator) {
		super();
		this.corePlanningProblem = corePlanningProblem;
		this.planEvaluator = planEvaluator;
	}

	public P getCorePlanningProblem() {
		return this.corePlanningProblem;
	}

	public IObjectEvaluator<Plan<A>, V> getPlanEvaluator() {
		return this.planEvaluator;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("corePlanningProblem", this.corePlanningProblem);
		fields.put("planEvaluator", this.planEvaluator);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
}

package ai.libs.jaicore.planning.hierarchical.problems.htn;

import java.util.HashMap;
import java.util.Map;

import org.api4.java.common.attributedobjects.IObjectEvaluator;

import ai.libs.jaicore.logging.ToJSONStringUtil;
import ai.libs.jaicore.logic.fol.structure.CNFFormula;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.planning.core.interfaces.IPlan;
import ai.libs.jaicore.planning.hierarchical.problems.stn.STNPlanningDomain;
import ai.libs.jaicore.planning.hierarchical.problems.stn.TaskNetwork;

public class CostSensitiveHTNPlanningProblem<P extends IHTNPlanningProblem, V extends Comparable<V>> implements IHTNPlanningProblem {
	private final P corePlanningProblem;
	private final IObjectEvaluator<IPlan, V> planEvaluator;

	public CostSensitiveHTNPlanningProblem(final P corePlanningProblem, final IObjectEvaluator<IPlan, V> planEvaluator) {
		super();
		this.corePlanningProblem = corePlanningProblem;
		this.planEvaluator = planEvaluator;
	}

	public P getCorePlanningProblem() {
		return this.corePlanningProblem;
	}

	public IObjectEvaluator<IPlan, V> getPlanEvaluator() {
		return this.planEvaluator;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("corePlanningProblem", this.corePlanningProblem);
		fields.put("planEvaluator", this.planEvaluator);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}

	@Override
	public STNPlanningDomain getDomain() {
		return this.corePlanningProblem.getDomain();
	}

	@Override
	public CNFFormula getKnowledge() {
		return this.corePlanningProblem.getKnowledge();
	}

	@Override
	public Monom getInit() {
		return this.corePlanningProblem.getInit();
	}

	@Override
	public TaskNetwork getNetwork() {
		return this.corePlanningProblem.getNetwork();
	}
}

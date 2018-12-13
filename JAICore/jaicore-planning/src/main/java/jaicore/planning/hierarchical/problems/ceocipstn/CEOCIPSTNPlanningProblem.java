package jaicore.planning.hierarchical.problems.ceocipstn;

import java.util.HashMap;
import java.util.Map;

import jaicore.logging.ToJSONStringUtil;
import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.theories.EvaluablePredicate;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.ceociptfd.OracleTaskResolver;
import jaicore.planning.hierarchical.problems.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.hierarchical.problems.stn.TaskNetwork;

@SuppressWarnings("serial")
public class CEOCIPSTNPlanningProblem extends CEOCSTNPlanningProblem {
	private final Map<String, EvaluablePredicate> evaluablePlanningPredicates;
	private final Map<String, OracleTaskResolver> oracleResolvers;

	public CEOCIPSTNPlanningProblem(CEOCIPSTNPlanningDomain domain, CNFFormula knowledge, Monom init, TaskNetwork network, Map<String, EvaluablePredicate> evaluablePredicates,
			Map<String, OracleTaskResolver> oracleResolvers) {
		super(domain, knowledge, init, network);
		this.evaluablePlanningPredicates = evaluablePredicates;
		this.oracleResolvers = oracleResolvers;
	}
	
	@Override
	public CEOCIPSTNPlanningDomain getDomain() {
		return (CEOCIPSTNPlanningDomain)super.getDomain();
	}

	public Map<String, EvaluablePredicate> getEvaluablePlanningPredicates() {
		return evaluablePlanningPredicates;
	}

	public Map<String, OracleTaskResolver> getOracleResolvers() {
		return oracleResolvers;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((evaluablePlanningPredicates == null) ? 0 : evaluablePlanningPredicates.hashCode());
		result = prime * result + ((oracleResolvers == null) ? 0 : oracleResolvers.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CEOCIPSTNPlanningProblem other = (CEOCIPSTNPlanningProblem) obj;
		if (evaluablePlanningPredicates == null) {
			if (other.evaluablePlanningPredicates != null)
				return false;
		} else if (!evaluablePlanningPredicates.equals(other.evaluablePlanningPredicates))
			return false;
		if (oracleResolvers == null) {
			if (other.oracleResolvers != null)
				return false;
		} else if (!oracleResolvers.equals(other.oracleResolvers))
			return false;
		return true;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("evaluablePlanningPredicates", this.evaluablePlanningPredicates);
		fields.put("oracleResolvers", this.oracleResolvers);
		fields.put("super", super.toString());
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
	
	
}

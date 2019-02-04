package jaicore.planning.model.task.ceocipstn;

import java.util.HashMap;
import java.util.Map;

import jaicore.logging.ToJSONStringUtil;
import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.theories.EvaluablePredicate;
import jaicore.planning.graphgenerators.task.ceociptfd.OracleTaskResolver;
import jaicore.planning.model.ceoc.CEOCAction;
import jaicore.planning.model.ceoc.CEOCOperation;
import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningDomain;
import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.model.task.stn.TaskNetwork;

@SuppressWarnings("serial")
public class CEOCIPSTNPlanningProblem<O extends CEOCOperation, M extends OCIPMethod, A extends CEOCAction> extends CEOCSTNPlanningProblem<O, M, A> {
	private final Map<String, EvaluablePredicate> evaluablePlanningPredicates;
	private final Map<String, OracleTaskResolver> oracleResolvers;

	public CEOCIPSTNPlanningProblem(final CEOCSTNPlanningDomain<O, M> domain, final CNFFormula knowledge, final Monom init, final TaskNetwork network, final Map<String, EvaluablePredicate> evaluablePredicates,
			final Map<String, OracleTaskResolver> oracleResolvers) {
		super(domain, knowledge, init, network);
		this.evaluablePlanningPredicates = evaluablePredicates;
		this.oracleResolvers = oracleResolvers;
	}

	public Map<String, EvaluablePredicate> getEvaluablePlanningPredicates() {
		return this.evaluablePlanningPredicates;
	}

	public Map<String, OracleTaskResolver> getOracleResolvers() {
		return this.oracleResolvers;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.evaluablePlanningPredicates == null) ? 0 : this.evaluablePlanningPredicates.hashCode());
		result = prime * result + ((this.oracleResolvers == null) ? 0 : this.oracleResolvers.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("unchecked")
		CEOCIPSTNPlanningProblem<O, M, A> other = (CEOCIPSTNPlanningProblem<O, M, A>) obj;
		if (this.evaluablePlanningPredicates == null) {
			if (other.evaluablePlanningPredicates != null) {
				return false;
			}
		} else if (!this.evaluablePlanningPredicates.equals(other.evaluablePlanningPredicates)) {
			return false;
		}
		if (this.oracleResolvers == null) {
			if (other.oracleResolvers != null) {
				return false;
			}
		} else if (!this.oracleResolvers.equals(other.oracleResolvers)) {
			return false;
		}
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

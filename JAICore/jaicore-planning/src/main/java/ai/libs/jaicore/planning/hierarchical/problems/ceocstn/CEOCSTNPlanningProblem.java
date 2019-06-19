package ai.libs.jaicore.planning.hierarchical.problems.ceocstn;

import ai.libs.jaicore.logic.fol.structure.CNFFormula;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.planning.hierarchical.problems.stn.STNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.stn.TaskNetwork;

@SuppressWarnings("serial")
public class CEOCSTNPlanningProblem extends STNPlanningProblem {

	public CEOCSTNPlanningProblem(CEOCSTNPlanningDomain domain, CNFFormula knowledge, Monom init, TaskNetwork network) {
		super(domain, knowledge, init, network);
	}
	
	@Override
	public CEOCSTNPlanningDomain getDomain() {
		return (CEOCSTNPlanningDomain)super.getDomain();
	}
}

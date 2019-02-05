package jaicore.planning.hierarchical.problems.ceocstn;

import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.hierarchical.problems.stn.STNPlanningProblem;
import jaicore.planning.hierarchical.problems.stn.TaskNetwork;

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

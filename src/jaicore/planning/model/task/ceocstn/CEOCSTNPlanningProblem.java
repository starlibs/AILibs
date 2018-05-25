package jaicore.planning.model.task.ceocstn;

import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.model.task.stn.STNPlanningProblem;
import jaicore.planning.model.task.stn.TaskNetwork;

@SuppressWarnings("serial")
public class CEOCSTNPlanningProblem extends STNPlanningProblem {

	public CEOCSTNPlanningProblem(CEOCSTNPlanningDomain domain, CNFFormula knowledge, Monom init, TaskNetwork network) {
		super(domain, knowledge, init, network);
	}

	public CEOCSTNPlanningDomain getDomain() {
		return (CEOCSTNPlanningDomain)super.getDomain();
	}
}

package jaicore.planning.model.task.ceocstn;

import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.model.ceoc.CEOCAction;
import jaicore.planning.model.ceoc.CEOCOperation;
import jaicore.planning.model.task.stn.STNPlanningProblem;
import jaicore.planning.model.task.stn.TaskNetwork;

@SuppressWarnings("serial")
public class CEOCSTNPlanningProblem<O extends CEOCOperation, M extends OCMethod, A extends CEOCAction> extends STNPlanningProblem<O, M, A> {

	public CEOCSTNPlanningProblem(CEOCSTNPlanningDomain<O,M> domain, CNFFormula knowledge, Monom init, TaskNetwork network) {
		super(domain, knowledge, init, network);
	}
}

package jaicore.planning.hierarchical.problems.ceocstn;

import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.classical.problems.ceoc.CEOCAction;
import jaicore.planning.classical.problems.ceoc.CEOCOperation;
import jaicore.planning.hierarchical.problems.stn.STNPlanningProblem;
import jaicore.planning.hierarchical.problems.stn.TaskNetwork;

@SuppressWarnings("serial")
public class CEOCSTNPlanningProblem<O extends CEOCOperation, M extends OCMethod, A extends CEOCAction> extends STNPlanningProblem<O, M, A> {

	public CEOCSTNPlanningProblem(CEOCSTNPlanningDomain<O,M> domain, CNFFormula knowledge, Monom init, TaskNetwork network) {
		super(domain, knowledge, init, network);
	}
}

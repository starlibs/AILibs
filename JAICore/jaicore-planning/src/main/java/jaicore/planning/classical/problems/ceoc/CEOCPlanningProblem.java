package jaicore.planning.classical.problems.ceoc;

import jaicore.logic.fol.structure.Monom;
import jaicore.planning.classical.problems.strips.PlanningProblem;

public class CEOCPlanningProblem extends PlanningProblem {

	public CEOCPlanningProblem(CEOCPlanningDomain domain, Monom initState, Monom goalState) {
		super(domain, initState, goalState);
	}

}

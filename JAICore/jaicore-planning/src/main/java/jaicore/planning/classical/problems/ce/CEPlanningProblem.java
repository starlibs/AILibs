package jaicore.planning.classical.problems.ce;

import jaicore.logic.fol.structure.Monom;
import jaicore.planning.classical.problems.strips.PlanningProblem;

public class CEPlanningProblem extends PlanningProblem {

	public CEPlanningProblem(CEPlanningDomain domain, Monom initState, Monom goalState) {
		super(domain, initState, goalState);
	}

}

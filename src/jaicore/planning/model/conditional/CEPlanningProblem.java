package jaicore.planning.model.conditional;

import jaicore.logic.Monom;
import jaicore.planning.model.core.PlanningProblem;

public class CEPlanningProblem extends PlanningProblem {

	public CEPlanningProblem(CEPlanningDomain domain, Monom initState, Monom goalState) {
		super(domain, initState, goalState);
	}

}

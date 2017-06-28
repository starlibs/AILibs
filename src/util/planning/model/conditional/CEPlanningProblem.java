package util.planning.model.conditional;

import util.logic.Monom;
import util.planning.model.core.PlanningProblem;

public class CEPlanningProblem extends PlanningProblem {

	public CEPlanningProblem(CEPlanningDomain domain, Monom initState, Monom goalState) {
		super(domain, initState, goalState);
	}

}

package util.planning.model.ceoc;

import util.logic.Monom;
import util.planning.model.core.PlanningProblem;

public class CEOCPlanningProblem extends PlanningProblem {

	public CEOCPlanningProblem(CEOCPlanningDomain domain, Monom initState, Monom goalState) {
		super(domain, initState, goalState);
	}

}

package jaicore.planning.model.ceoc;

import jaicore.logic.Monom;
import jaicore.planning.model.core.PlanningProblem;

public class CEOCPlanningProblem extends PlanningProblem {

	public CEOCPlanningProblem(CEOCPlanningDomain domain, Monom initState, Monom goalState) {
		super(domain, initState, goalState);
	}

}

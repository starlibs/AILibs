package jaicore.planning.model.strips;

import jaicore.logic.Monom;
import jaicore.planning.model.core.PlanningProblem;

public class StripsPlanningProblem extends PlanningProblem {

	public StripsPlanningProblem(StripsPlanningDomain domain, Monom initState, Monom goalState) {
		super(domain, initState, goalState);
	}

}

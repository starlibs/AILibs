package util.planning.model.strips;

import util.logic.Monom;
import util.planning.model.core.PlanningProblem;

public class StripsPlanningProblem extends PlanningProblem {

	public StripsPlanningProblem(StripsPlanningDomain domain, Monom initState, Monom goalState) {
		super(domain, initState, goalState);
	}

}

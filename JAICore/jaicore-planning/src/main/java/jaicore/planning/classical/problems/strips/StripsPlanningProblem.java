package jaicore.planning.classical.problems.strips;

import jaicore.logic.fol.structure.Monom;

public class StripsPlanningProblem extends PlanningProblem {

	public StripsPlanningProblem(StripsPlanningProblem problem) {
		this(problem.getDomain(), problem.getInitState(), problem.getGoalStateFunction());
	}
	
	public StripsPlanningProblem(StripsPlanningDomain domain, Monom initState, Monom goalState) {
		super(domain, initState, goalState);
	}

	public StripsPlanningProblem(StripsPlanningDomain domain, Monom initState, GoalStateFunction goalChecker) {
		super(domain, initState, goalChecker);
	}
	
	public StripsPlanningDomain getDomain() {
		return (StripsPlanningDomain)super.getDomain();
	}
}

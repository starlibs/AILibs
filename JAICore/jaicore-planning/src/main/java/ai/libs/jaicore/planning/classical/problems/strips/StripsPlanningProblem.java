package ai.libs.jaicore.planning.classical.problems.strips;

import ai.libs.jaicore.logic.fol.structure.Monom;

public class StripsPlanningProblem extends PlanningProblem {

	public StripsPlanningProblem(final StripsPlanningProblem problem) {
		this(problem.getDomain(), problem.getInitState(), problem.getGoalStateFunction());
	}

	public StripsPlanningProblem(final StripsPlanningDomain domain, final Monom initState, final Monom goalState) {
		super(domain, initState, goalState);
	}

	public StripsPlanningProblem(final StripsPlanningDomain domain, final Monom initState, final GoalStateFunction goalChecker) {
		super(domain, initState, goalChecker);
	}

	@Override
	public StripsPlanningDomain getDomain() {
		return (StripsPlanningDomain) super.getDomain();
	}
}

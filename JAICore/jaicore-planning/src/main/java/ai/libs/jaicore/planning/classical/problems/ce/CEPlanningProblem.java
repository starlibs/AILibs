package ai.libs.jaicore.planning.classical.problems.ce;

import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.planning.classical.problems.strips.PlanningProblem;

public class CEPlanningProblem extends PlanningProblem {

	public CEPlanningProblem(CEPlanningDomain domain, Monom initState, Monom goalState) {
		super(domain, initState, goalState);
	}

}

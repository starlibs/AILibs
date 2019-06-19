package ai.libs.jaicore.planning.classical.problems.ceoc;

import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.planning.classical.problems.strips.PlanningProblem;

public class CEOCPlanningProblem extends PlanningProblem {

	public CEOCPlanningProblem(CEOCPlanningDomain domain, Monom initState, Monom goalState) {
		super(domain, initState, goalState);
	}

}

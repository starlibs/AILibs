package ai.libs.jaicore.planning.hierarchical.problems.htn;

import java.io.Serializable;

import ai.libs.jaicore.logic.fol.structure.CNFFormula;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.planning.hierarchical.problems.stn.STNPlanningDomain;
import ai.libs.jaicore.planning.hierarchical.problems.stn.TaskNetwork;

public interface IHTNPlanningProblem extends Serializable {

	public STNPlanningDomain getDomain();

	public CNFFormula getKnowledge();

	public Monom getInit();

	public TaskNetwork getNetwork();
}

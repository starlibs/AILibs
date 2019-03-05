package jaicore.planning.hierarchical.problems.htn;

import java.io.Serializable;

import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.hierarchical.problems.stn.STNPlanningDomain;
import jaicore.planning.hierarchical.problems.stn.TaskNetwork;

public interface IHTNPlanningProblem extends Serializable {

	public STNPlanningDomain getDomain();

	public CNFFormula getKnowledge();

	public Monom getInit();

	public TaskNetwork getNetwork();
}

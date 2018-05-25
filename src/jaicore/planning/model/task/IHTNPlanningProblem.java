package jaicore.planning.model.task;

import java.io.Serializable;

import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.model.task.stn.STNPlanningDomain;
import jaicore.planning.model.task.stn.TaskNetwork;

public interface IHTNPlanningProblem extends Serializable {
	
	public STNPlanningDomain getDomain();
	
	public CNFFormula getKnowledge();

	public Monom getInit();

	public TaskNetwork getNetwork();
}

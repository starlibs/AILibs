package jaicore.planning.model.task;

import java.io.Serializable;

import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.task.stn.Method;
import jaicore.planning.model.task.stn.STNPlanningDomain;
import jaicore.planning.model.task.stn.TaskNetwork;

public interface IHTNPlanningProblem<O extends Operation, M extends Method, A extends Action> extends Serializable {

	public STNPlanningDomain<O, M> getDomain();

	public CNFFormula getKnowledge();

	public Monom getInit();

	public TaskNetwork getNetwork();
}

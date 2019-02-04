package jaicore.planning.hierarchical.problems.htn;

import java.io.Serializable;

import jaicore.logic.fol.structure.CNFFormula;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.classical.problems.strips.Operation;
import jaicore.planning.core.Action;
import jaicore.planning.hierarchical.problems.stn.Method;
import jaicore.planning.hierarchical.problems.stn.STNPlanningDomain;
import jaicore.planning.hierarchical.problems.stn.TaskNetwork;

public interface IHTNPlanningProblem<O extends Operation, M extends Method, A extends Action> extends Serializable {

	public STNPlanningDomain<O, M> getDomain();

	public CNFFormula getKnowledge();

	public Monom getInit();

	public TaskNetwork getNetwork();
}

package hasco.core;

import jaicore.planning.classical.problems.ceoc.CEOCAction;
import jaicore.planning.classical.problems.ceoc.CEOCOperation;
import jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import jaicore.planning.hierarchical.problems.ceocipstn.OCIPMethod;
import jaicore.planning.hierarchical.problems.htn.IHierarchicalPlanningGraphGeneratorDeriver;

public interface IHASCOPlanningGraphGeneratorDeriver<N, A>
		extends IHierarchicalPlanningGraphGeneratorDeriver<CEOCOperation, OCIPMethod, CEOCAction, CEOCIPSTNPlanningProblem<CEOCOperation, OCIPMethod, CEOCAction>, N, A> {

}

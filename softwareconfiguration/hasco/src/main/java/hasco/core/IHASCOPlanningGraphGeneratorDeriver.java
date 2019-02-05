package hasco.core;

import jaicore.planning.classical.problems.ceoc.CEOCAction;
import jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import jaicore.planning.hierarchical.problems.htn.IHierarchicalPlanningGraphGeneratorDeriver;

public interface IHASCOPlanningGraphGeneratorDeriver<N, A>
		extends IHierarchicalPlanningGraphGeneratorDeriver<CEOCAction, CEOCIPSTNPlanningProblem, N, A> {

}

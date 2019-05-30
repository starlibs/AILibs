package hasco.core;

import jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import jaicore.planning.hierarchical.problems.htn.IHierarchicalPlanningGraphGeneratorDeriver;

public interface IHASCOPlanningGraphGeneratorDeriver<N, A>
		extends IHierarchicalPlanningGraphGeneratorDeriver<CEOCIPSTNPlanningProblem, N, A> {

}

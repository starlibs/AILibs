package ai.libs.hasco.core;

import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHierarchicalPlanningGraphGeneratorDeriver;

public interface IHASCOPlanningGraphGeneratorDeriver<N, A>
		extends IHierarchicalPlanningGraphGeneratorDeriver<CEOCIPSTNPlanningProblem, N, A> {

}

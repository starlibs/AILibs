package ai.libs.hasco.core;

import ai.libs.jaicore.planning.core.interfaces.IPlan;
import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHierarchicalPlanningToGraphSearchReduction;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public interface IHASCOPlanningReduction<N, A> extends IHierarchicalPlanningToGraphSearchReduction<N, A, CEOCIPSTNPlanningProblem, IPlan, GraphSearchInput<N,A>, SearchGraphPath<N,A>> {

}

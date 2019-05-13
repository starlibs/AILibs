package ai.libs.jaicore.planning.hierarchical.problems.htn;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.planning.core.Plan;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public interface IHierarchicalPlanningGraphGeneratorDeriver<P extends IHTNPlanningProblem, N, A> extends AlgorithmicProblemReduction<P, Plan, GraphSearchInput<N, A>, SearchGraphPath<N, A>> {

}

package ai.libs.jaicore.planning.classical.algorithms.strips.forward;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.planning.classical.problems.strips.StripsPlanningProblem;
import ai.libs.jaicore.planning.core.Plan;
import ai.libs.jaicore.search.core.interfaces.GraphGenerator;
import ai.libs.jaicore.search.model.other.SearchGraphPath;

public interface ISTRIPSPlanningGraphGeneratorDeriver<N, A> extends AlgorithmicProblemReduction<StripsPlanningProblem, Plan, GraphGenerator<N, A>, SearchGraphPath<N, A>> {

}

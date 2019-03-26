package jaicore.planning.classical.algorithms.strips.forward;

import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.planning.classical.problems.strips.StripsPlanningProblem;
import jaicore.planning.core.Plan;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.other.SearchGraphPath;

public interface ISTRIPSPlanningGraphGeneratorDeriver<N, A> extends AlgorithmicProblemReduction<StripsPlanningProblem, Plan, GraphGenerator<N, A>, SearchGraphPath<N, A>> {

}

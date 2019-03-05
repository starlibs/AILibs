package jaicore.planning.classical.algorithms.strips.forward;

import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.planning.classical.problems.strips.StripsPlanningProblem;
import jaicore.planning.hierarchical.algorithms.IPathToPlanConverter;
import jaicore.search.core.interfaces.GraphGenerator;

public interface ISTRIPSPlanningGraphGeneratorDeriver<N, A> extends AlgorithmicProblemReduction<StripsPlanningProblem, GraphGenerator<N, A>>, IPathToPlanConverter<N> {

}

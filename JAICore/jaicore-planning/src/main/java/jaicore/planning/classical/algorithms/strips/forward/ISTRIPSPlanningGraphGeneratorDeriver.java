package jaicore.planning.classical.algorithms.strips.forward;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.planning.classical.problems.strips.StripsPlanningProblem;
import jaicore.planning.core.Action;
import jaicore.planning.hierarchical.algorithms.IPathToPlanConverter;
import jaicore.search.core.interfaces.GraphGenerator;

public interface ISTRIPSPlanningGraphGeneratorDeriver<PA extends Action, N, A> extends AlgorithmProblemTransformer<StripsPlanningProblem, GraphGenerator<N, A>>, IPathToPlanConverter<N, PA> {

}

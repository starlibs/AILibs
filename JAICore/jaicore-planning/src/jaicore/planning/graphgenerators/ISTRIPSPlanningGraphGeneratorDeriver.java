package jaicore.planning.graphgenerators;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.planning.algorithms.IPathToPlanConverter;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.strips.StripsPlanningProblem;
import jaicore.search.core.interfaces.GraphGenerator;

public interface ISTRIPSPlanningGraphGeneratorDeriver<PA extends Action, N, A> extends AlgorithmProblemTransformer<StripsPlanningProblem, GraphGenerator<N, A>>, IPathToPlanConverter<N, PA> {

}

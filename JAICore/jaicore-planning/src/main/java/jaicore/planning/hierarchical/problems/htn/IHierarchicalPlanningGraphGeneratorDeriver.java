package jaicore.planning.hierarchical.problems.htn;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.planning.core.Action;
import jaicore.planning.hierarchical.algorithms.IPathToPlanConverter;
import jaicore.search.core.interfaces.GraphGenerator;

public interface IHierarchicalPlanningGraphGeneratorDeriver<PA extends Action, N, A> extends AlgorithmProblemTransformer<IHTNPlanningProblem, GraphGenerator<N, A>>, IPathToPlanConverter<N, PA> {

}

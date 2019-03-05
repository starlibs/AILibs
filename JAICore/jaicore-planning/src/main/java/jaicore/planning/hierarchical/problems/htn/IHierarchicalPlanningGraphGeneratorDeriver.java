package jaicore.planning.hierarchical.problems.htn;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.planning.hierarchical.algorithms.IPathToPlanConverter;
import jaicore.search.core.interfaces.GraphGenerator;

public interface IHierarchicalPlanningGraphGeneratorDeriver<IPlanning extends IHTNPlanningProblem, N, A> extends AlgorithmProblemTransformer<IPlanning, GraphGenerator<N, A>>, IPathToPlanConverter<N> {

}

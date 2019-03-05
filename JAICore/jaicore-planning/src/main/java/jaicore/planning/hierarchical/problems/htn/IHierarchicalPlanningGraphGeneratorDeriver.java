package jaicore.planning.hierarchical.problems.htn;

import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.planning.hierarchical.algorithms.IPathToPlanConverter;
import jaicore.search.core.interfaces.GraphGenerator;

public interface IHierarchicalPlanningGraphGeneratorDeriver<IPlanning extends IHTNPlanningProblem, N, A> extends AlgorithmicProblemReduction<IPlanning, GraphGenerator<N, A>>, IPathToPlanConverter<N> {

}

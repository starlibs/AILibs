package jaicore.planning.hierarchical.problems.htn;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.planning.classical.problems.strips.Operation;
import jaicore.planning.core.Action;
import jaicore.planning.hierarchical.algorithms.IPathToPlanConverter;
import jaicore.planning.hierarchical.problems.stn.Method;
import jaicore.search.core.interfaces.GraphGenerator;

public interface IHierarchicalPlanningGraphGeneratorDeriver<PO extends Operation,PM extends Method,PA extends Action, I extends IHTNPlanningProblem<PO, PM, PA>, N, A> extends AlgorithmProblemTransformer<I, GraphGenerator<N, A>>, IPathToPlanConverter<N, PA> {

}

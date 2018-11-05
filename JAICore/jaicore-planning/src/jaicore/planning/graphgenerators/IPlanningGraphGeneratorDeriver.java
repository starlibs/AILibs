package jaicore.planning.graphgenerators;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.planning.algorithms.IPathToPlanConverter;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.task.IHTNPlanningProblem;
import jaicore.planning.model.task.stn.Method;
import jaicore.search.core.interfaces.GraphGenerator;

public interface IPlanningGraphGeneratorDeriver<PO extends Operation,PM extends Method,PA extends Action, I extends IHTNPlanningProblem<PO, PM, PA>, N, A> extends AlgorithmProblemTransformer<I, GraphGenerator<N, A>>, IPathToPlanConverter<N, PA> {

}

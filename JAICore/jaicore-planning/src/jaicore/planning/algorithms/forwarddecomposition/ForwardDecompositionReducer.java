package jaicore.planning.algorithms.forwarddecomposition;

import java.util.List;
import java.util.stream.Collectors;

import jaicore.planning.graphgenerators.IPlanningGraphGeneratorDeriver;
import jaicore.planning.graphgenerators.task.ceociptfd.CEOCIPTFDGraphGenerator;
import jaicore.planning.graphgenerators.task.ceoctfd.CEOCTFDGraphGenerator;
import jaicore.planning.graphgenerators.task.tfd.TFDGraphGenerator;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.ceoc.CEOCAction;
import jaicore.planning.model.ceoc.CEOCOperation;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Operation;
import jaicore.planning.model.core.Plan;
import jaicore.planning.model.task.IHTNPlanningProblem;
import jaicore.planning.model.task.ceocipstn.CEOCIPSTNPlanningProblem;
import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.model.task.ceocstn.OCMethod;
import jaicore.planning.model.task.stn.Method;
import jaicore.planning.model.task.stn.STNPlanningProblem;
import jaicore.search.core.interfaces.GraphGenerator;

public class ForwardDecompositionReducer<O extends Operation, M extends Method, A extends Action, I extends IHTNPlanningProblem<O, M, A>> implements IPlanningGraphGeneratorDeriver<O, M, A, I, TFDNode, String> {

	@Override
	public GraphGenerator<TFDNode, String> transform(I planningProblem) {
		GraphGenerator<TFDNode, String> graphGenerator;
		if (planningProblem instanceof CEOCIPSTNPlanningProblem) {
			graphGenerator = new CEOCIPTFDGraphGenerator((CEOCIPSTNPlanningProblem<? extends CEOCOperation,? extends OCMethod, ? extends CEOCAction>) planningProblem);
		} else if (planningProblem instanceof CEOCSTNPlanningProblem) {
			graphGenerator = new CEOCTFDGraphGenerator<>((CEOCSTNPlanningProblem<? extends CEOCOperation,? extends OCMethod, ? extends CEOCAction>) planningProblem);
		} else if (planningProblem.getClass().equals(STNPlanningProblem.class)) {
			graphGenerator = new TFDGraphGenerator<>(planningProblem);
		} else {
			throw new IllegalArgumentException("HTN problems of class \"" + planningProblem.getClass().getName() + "\" are currently not supported.");
		}
		return graphGenerator;
	}
	
	@Override
	public Plan<A> getPlan(List<TFDNode> path) {
		return new Plan<>(path.stream().filter(n -> n.getAppliedAction() != null).map(n -> (A) n.getAppliedAction()).collect(Collectors.toList()));
	}
}

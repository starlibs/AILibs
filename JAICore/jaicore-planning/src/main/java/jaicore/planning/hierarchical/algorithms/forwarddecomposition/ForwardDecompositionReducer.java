package jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import java.util.List;
import java.util.stream.Collectors;

import jaicore.planning.classical.problems.ceoc.CEOCAction;
import jaicore.planning.classical.problems.ceoc.CEOCOperation;
import jaicore.planning.classical.problems.strips.Operation;
import jaicore.planning.core.Action;
import jaicore.planning.core.Plan;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.ceociptfd.CEOCIPTFDGraphGenerator;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.ceoctfd.CEOCTFDGraphGenerator;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDGraphGenerator;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import jaicore.planning.hierarchical.problems.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.hierarchical.problems.ceocstn.OCMethod;
import jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import jaicore.planning.hierarchical.problems.htn.IHierarchicalPlanningGraphGeneratorDeriver;
import jaicore.planning.hierarchical.problems.stn.Method;
import jaicore.planning.hierarchical.problems.stn.STNPlanningProblem;
import jaicore.search.core.interfaces.GraphGenerator;

public class ForwardDecompositionReducer<O extends Operation, M extends Method, A extends Action, I extends IHTNPlanningProblem<O, M, A>> implements IHierarchicalPlanningGraphGeneratorDeriver<O, M, A, I, TFDNode, String> {

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

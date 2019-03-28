package jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import java.util.stream.Collectors;

import jaicore.planning.core.Plan;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.ceociptfd.CEOCIPTFDGraphGenerator;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.ceoctfd.CEOCTFDGraphGenerator;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDGraphGenerator;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import jaicore.planning.hierarchical.problems.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import jaicore.planning.hierarchical.problems.htn.IHierarchicalPlanningGraphGeneratorDeriver;
import jaicore.planning.hierarchical.problems.stn.STNPlanningProblem;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchInput;

public class ForwardDecompositionReducer<IPlanning extends IHTNPlanningProblem> implements IHierarchicalPlanningGraphGeneratorDeriver<IPlanning, TFDNode, String> {

	@Override
	public GraphSearchInput<TFDNode, String> encodeProblem(final IHTNPlanningProblem planningProblem) {
		GraphGenerator<TFDNode, String> graphGenerator;
		if (planningProblem instanceof CEOCIPSTNPlanningProblem) {
			graphGenerator = new CEOCIPTFDGraphGenerator((CEOCIPSTNPlanningProblem) planningProblem);
		} else if (planningProblem instanceof CEOCSTNPlanningProblem) {
			graphGenerator = new CEOCTFDGraphGenerator((CEOCSTNPlanningProblem) planningProblem);
		} else if (planningProblem.getClass().equals(STNPlanningProblem.class)) {
			graphGenerator = new TFDGraphGenerator(planningProblem);
		} else {
			throw new IllegalArgumentException("HTN problems of class \"" + planningProblem.getClass().getName() + "\" are currently not supported.");
		}
		return new GraphSearchInput<>(graphGenerator);
	}

	@Override
	public Plan decodeSolution(final SearchGraphPath<TFDNode, String> solution) {
		return new Plan(solution.getNodes().stream().filter(n -> n.getAppliedAction() != null).map(TFDNode::getAppliedAction).collect(Collectors.toList()));
	}
}

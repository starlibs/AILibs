package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition;

import java.util.stream.Collectors;

import org.api4.java.datastructure.graph.ILabeledPath;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;

import ai.libs.jaicore.planning.core.Plan;
import ai.libs.jaicore.planning.core.interfaces.IPlan;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.ceociptfd.CEOCIPTFDGraphGenerator;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.ceoctfd.CEOCTFDGraphGenerator;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDGraphGenerator;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.ceocstn.CEOCSTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHierarchicalPlanningToGraphSearchReduction;
import ai.libs.jaicore.planning.hierarchical.problems.stn.STNPlanningProblem;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

public abstract class AForwardDecompositionReducer<I1 extends IHTNPlanningProblem, O1 extends IPlan, I2 extends GraphSearchInput<TFDNode, String>, O2 extends ILabeledPath<TFDNode, String>> implements IHierarchicalPlanningToGraphSearchReduction<TFDNode, String, I1, O1, I2, O2> {

	public GraphSearchInput<TFDNode, String> getGraphSearchInput(final I1 planningProblem) {
		IGraphGenerator<TFDNode, String> graphGenerator;
		if (planningProblem instanceof CEOCIPSTNPlanningProblem) {
			graphGenerator = new CEOCIPTFDGraphGenerator((CEOCIPSTNPlanningProblem) planningProblem);
		} else if (planningProblem instanceof CEOCSTNPlanningProblem) {
			graphGenerator = new CEOCTFDGraphGenerator((CEOCSTNPlanningProblem) planningProblem);
		} else if (planningProblem.getClass().equals(STNPlanningProblem.class)) {
			graphGenerator = new TFDGraphGenerator(planningProblem);
		} else {
			throw new IllegalArgumentException("HTN problems of class \"" + planningProblem.getClass().getName() + "\" are currently not supported.");
		}
		return new GraphSearchInput<>(graphGenerator, l -> l.getHead().getRemainingTasks().isEmpty());
	}

	public Plan getPlanForSolution(final ILabeledPath<TFDNode, String> solution) {
		return new Plan(solution.getNodes().stream().filter(n -> n.getAppliedAction() != null).map(TFDNode::getAppliedAction).collect(Collectors.toList()));
	}
}

package jaicore.planning.task;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionHTNPlanner;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionHTNPlanner.SolutionIterator;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.model.task.ceocstn.StandardProblemFactory;
import jaicore.search.structure.core.Node;

public class ForwardDecompositionTest {

	@Test
	public void test() {
		
		/* create nested dichotomy problem */
		Collection<String> init = Arrays.asList(new String[] {"A", "B", "C", "D"});
		CEOCSTNPlanningProblem problem = StandardProblemFactory.getNestedDichotomyCreationProblem("root", init, true, 0, 0);
		ForwardDecompositionHTNPlanner planner = new ForwardDecompositionHTNPlanner(problem, 1);
		SolutionIterator plannerRun = planner.iterator();
		new SimpleGraphVisualizationWindow<Node<TFDNode,Double>>(plannerRun.getSearch()).getPanel().setTooltipGenerator(new TFDTooltipGenerator<>());
		
		/* solve problem */
		System.out.println("Starting search. Waiting for solutions:");
		while (plannerRun.hasNext()) {
			List<TFDNode> solution = plannerRun.next();
			System.out.println("\t" + solution);
		}
		System.out.println("Algorithm has finished.");
	}

}

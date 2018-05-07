package jaicore.graphvisualizer.gui;

import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionHTNPlanner;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.model.task.ceocstn.StandardProblemFactory;
import jaicore.search.graphgenerators.nqueens.QueenNode;
import jaicore.search.structure.core.Node;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class FXGuiTester {


	@Test
	public void test() {
		/* create nested dichotomy problem */
		Collection<String> init = Arrays.asList(new String[] {"A", "B", "C", "D"});
		CEOCSTNPlanningProblem problem = StandardProblemFactory.getNestedDichotomyCreationProblem("root", init, true, 0, 0);
		ForwardDecompositionHTNPlanner planner = new ForwardDecompositionHTNPlanner(problem, 1);
		ForwardDecompositionHTNPlanner.SolutionIterator plannerRun = planner.iterator();
//		new SimpleGraphVisualizationWindow<Node<TFDNode,Double>>(plannerRun.getSearch()).getPanel().setTooltipGenerator(new TFDTooltipGenerator<>());


		Recorder<Node<TFDNode,Double>> recorder = new Recorder<>(plannerRun.getSearch());
		recorder.setTooltipGenerator(new TFDTooltipGenerator<>());

		/* solve problem */
		System.out.println("Starting search. Waiting for solutions:");
		while (plannerRun.hasNext()) {
			List<TFDNode> solution = (List<TFDNode>) plannerRun.next();
			System.out.println("\t" + solution);
		}
		System.out.println("Algorithm has finished.");






		String [] args = new String[0];
		//FXGui2.setRec(recorder);
		FXController.setRec(recorder);
		System.out.println("Start the GUI");
		javafx.application.Application.launch(FXGui.class, args);


	}

}

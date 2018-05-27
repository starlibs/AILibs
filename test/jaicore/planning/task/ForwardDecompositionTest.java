package jaicore.planning.task;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import jaicore.basic.MathExt;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionHTNPlanner;
import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.model.task.ceocstn.StandardProblemFactory;

public class ForwardDecompositionTest {

	private void solveNDProblem(int numClasses) {
		List<String> classes = new ArrayList<>();
		for (int i = 1; i <= numClasses; i++)
			classes.add("C" + i);
			
		CEOCSTNPlanningProblem problem = StandardProblemFactory.getNestedDichotomyCreationProblem("root", classes, true, 0, 0);
		ForwardDecompositionHTNPlanner<Double> planner = new ForwardDecompositionHTNPlanner<>(problem, n -> 0.0, 1, 1);
		ForwardDecompositionHTNPlanner<Double>.SolutionIterator plannerRun = planner.iterator();
//		new SimpleGraphVisualizationWindow<Node<TFDNode,Double>>(plannerRun.getSearch()).getPanel().setTooltipGenerator(new TFDTooltipGenerator<>());
		
		/* solve problem */
		System.out.println("Searching all nested dichotomies to sepratate " + numClasses + ".");
		int numSolutions = 0;
		while (plannerRun.hasNext()) {
			plannerRun.next();
			numSolutions ++;
			if (numSolutions % 100 == 0)
				System.out.println("Found " + numSolutions + " solutions so far ...");
		}
		int numberOfExpectedDichotomies = MathExt.doubleFactorial(2 * numClasses - 3);
		assertEquals(numberOfExpectedDichotomies, numSolutions);
		System.out.println("Ready, found exactly the expected " + numberOfExpectedDichotomies + " solutions.");
	}
	
	@Test
	public void solveNDWith3Classes() {
		solveNDProblem(3);
	}
	
	@Test
	public void solveNDWith4Classes() {
		solveNDProblem(4);
	}
	
	@Test
	public void solveNDWith5Classes() {
		solveNDProblem(5);
	}

}

package jaicore.planning.hierarchical.testproblems.nesteddichotomies;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import jaicore.basic.MathExt;
import jaicore.basic.algorithm.HomogeneousGeneralAlgorithmTester;
import jaicore.basic.algorithm.IAlgorithmFactory;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.planning.core.EvaluatedSearchGraphBasedPlan;
import jaicore.planning.core.events.PlanFoundEvent;
import jaicore.planning.hierarchical.algorithms.GraphSearchBasedHTNPlanningAlgorithm;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.planning.hierarchical.problems.ceocstn.CEOCSTNPlanningProblem;

public abstract class CEOCSTNNestedDichotomyTest extends HomogeneousGeneralAlgorithmTester<CEOCSTNPlanningProblem, EvaluatedSearchGraphBasedPlan<Double, TFDNode>> {
	
	private void checkNumberOfSolutionsForProblemSize(int numClasses) {
			
		CEOCSTNPlanningProblem problem = CEOCSTNNDProblemGenerator.getNestedDichotomyCreationProblem("root", numClasses, true, 0, 0);
		IAlgorithmFactory<CEOCSTNPlanningProblem, EvaluatedSearchGraphBasedPlan<Double, TFDNode>> factory = getFactory();
		factory.setProblemInput(problem);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		GraphSearchBasedHTNPlanningAlgorithm<CEOCSTNPlanningProblem, ?, ?, ?, Double> planner = (GraphSearchBasedHTNPlanningAlgorithm)factory.getAlgorithm();
		
		/* solve problem */
		System.out.println("Searching all nested dichotomies to sepratate " + numClasses + ".");
		int numSolutions = 0;
		for (AlgorithmEvent ae : planner) {
			if (ae instanceof PlanFoundEvent) {
				numSolutions ++;
				if (numSolutions % 10 == 0)
					System.out.println("Found " + numSolutions + " solutions so far ...");
			}
		}
		int numberOfExpectedDichotomies = MathExt.doubleFactorial(2 * numClasses - 3);
		assertEquals(numberOfExpectedDichotomies, numSolutions);
		System.out.println("Ready, found exactly the expected " + numberOfExpectedDichotomies + " solutions.");
	}
	
	@Test
	public void solveNDWith3Classes() {
		checkNumberOfSolutionsForProblemSize(3);
	}
	
	@Test
	public void solveNDWith4Classes() {
		checkNumberOfSolutionsForProblemSize(4);
	}
	
	@Test
	public void solveNDWith5Classes() {
		checkNumberOfSolutionsForProblemSize(5);
	}

	@Override
	public CEOCSTNPlanningProblem getSimpleProblemInputForGeneralTestPurposes() {
		return CEOCSTNNDProblemGenerator.getNestedDichotomyCreationProblem("root", 3, true, 0, 0);
	}

	@Override
	public CEOCSTNPlanningProblem getDifficultProblemInputForGeneralTestPurposes() throws Exception {
		return CEOCSTNNDProblemGenerator.getNestedDichotomyCreationProblem("root", 10, true, 0, 0);
	}

}

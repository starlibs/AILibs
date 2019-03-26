package jaicore.planning.hierarchical.testproblems.nesteddichotomies;

import jaicore.basic.algorithm.AAlgorithmTestProblemSet;
import jaicore.planning.hierarchical.problems.ceocstn.CEOCSTNPlanningProblem;

public class CEOCSTNNestedDichotomyProblemSet extends AAlgorithmTestProblemSet<CEOCSTNPlanningProblem>  {

	public CEOCSTNNestedDichotomyProblemSet() {
		super("Nested Dichotomies");
	}

	//		private void checkNumberOfSolutionsForProblemSize(final int numClasses) {
	//
	//			CEOCSTNPlanningProblem problem = CEOCSTNNDProblemGenerator.getNestedDichotomyCreationProblem("root", numClasses, true, 0, 0);
	//			IAlgorithmFactory<CEOCSTNPlanningProblem, EvaluatedSearchGraphBasedPlan<Double, TFDNode>> factory = getFactory();
	//			factory.setProblemInput(problem);
	//			@SuppressWarnings({ "rawtypes", "unchecked" })
	//			GraphSearchBasedHTNPlanningAlgorithm<CEOCSTNPlanningProblem, ?, ?, ?, Double> planner = (GraphSearchBasedHTNPlanningAlgorithm)factory.getAlgorithm();
	//
	//			/* solve problem */
	//			System.out.println("Searching all nested dichotomies to sepratate " + numClasses + ".");
	//			int numSolutions = 0;
	//			for (AlgorithmEvent ae : planner) {
	//				if (ae instanceof PlanFoundEvent) {
	//					numSolutions ++;
	//					if (numSolutions % 10 == 0) {
	//						System.out.println("Found " + numSolutions + " solutions so far ...");
	//					}
	//				}
	//			}
	//			int numberOfExpectedDichotomies = MathExt.doubleFactorial(2 * numClasses - 3);
	//			assertEquals(numberOfExpectedDichotomies, numSolutions);
	//			System.out.println("Ready, found exactly the expected " + numberOfExpectedDichotomies + " solutions.");
	//		}
	//
	//		@Test
	//		public void solveNDWith3Classes() {
	//			this.checkNumberOfSolutionsForProblemSize(3);
	//		}
	//
	//		@Test
	//		public void solveNDWith4Classes() {
	//			this.checkNumberOfSolutionsForProblemSize(4);
	//		}
	//
	//		@Test
	//		public void solveNDWith5Classes() {
	//			this.checkNumberOfSolutionsForProblemSize(5);
	//		}

	@Override
	public CEOCSTNPlanningProblem getSimpleProblemInputForGeneralTestPurposes() {
		return CEOCSTNNDProblemGenerator.getNestedDichotomyCreationProblem("root", 3, true, 0, 0);
	}

	@Override
	public CEOCSTNPlanningProblem getDifficultProblemInputForGeneralTestPurposes() {
		return CEOCSTNNDProblemGenerator.getNestedDichotomyCreationProblem("root", 10, true, 0, 0);
	}

}

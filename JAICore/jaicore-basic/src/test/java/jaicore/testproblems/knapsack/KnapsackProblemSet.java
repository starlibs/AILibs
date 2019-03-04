package jaicore.testproblems.knapsack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.AlgorithmTestProblemSetForSolutionIterators;

public class KnapsackProblemSet extends AlgorithmTestProblemSetForSolutionIterators<KnapsackProblem, Set<String>>  {

	public KnapsackProblemSet() {
		super("Knapsack");
	}

	private static final Logger logger = LoggerFactory.getLogger(KnapsackProblemSet.class);
	
	private Map<String, Double> weights;
	private Map<String, Double> values;
	private Map<Set<String>, Double> bonusPoints;
	
//	public KnapsackProblem getProblem() {
//		
//		/* create knapsack problem */
//		Set<String> objects = new HashSet<>();
//		for (int i = 0; i < 10; i++) {
//			objects.add(String.valueOf(i));
//		}
//		weights = new HashMap<>();
//		weights.put("0", 23.0d);
//		weights.put("1", 31.0d);
//		weights.put("2", 29.0d);
//		weights.put("3", 44.0d);
//		weights.put("4", 53.0d);
//		weights.put("5", 38.0d);
//		weights.put("6", 63.0d);
//		weights.put("7", 85.0d);
//		weights.put("8", 89.0d);
//		weights.put("9", 82.0d);
//		values = new HashMap<>();
//		values.put("0", 92.0d);
//		values.put("1", 57.0d);
//		values.put("2", 49.0d);
//		values.put("3", 68.0d);
//		values.put("4", 60.0d);
//		values.put("5", 43.0d);
//		values.put("6", 67.0d);
//		values.put("7", 84.0d);
//		values.put("8", 87.0d);
//		values.put("9", 72.0d);
//		bonusPoints = new HashMap<>();
//		Set<String> bonusCombination = new HashSet<>();
//		bonusCombination.add("0");
//		bonusCombination.add("2");
//		bonusPoints.put(bonusCombination, 25.0d);
//		return new KnapsackProblem(objects, values, weights, bonusPoints, 165);
//	}
	

	public double getValueOfKnapsack(KnapsackConfiguration knapsack) {
		if (knapsack == null || knapsack.getPackedObjects() == null || knapsack.getPackedObjects().size() == 0) {
			return 0.0d;
		} else {
			double value = 0.0d;
			for (String object : knapsack.getPackedObjects()) {
				value += values.get(object);
			}
			for (Set<String> bonusCombination : bonusPoints.keySet()) {
				boolean allContained = true;
				for (String object : bonusCombination) {
					if (!knapsack.getPackedObjects().contains(object)) {
						allContained = false;
						break;
					}
				}
				if (allContained) {
					value += bonusPoints.get(bonusCombination);
				}
			}
			return value;
		}
	}
	
//	@Test
//	public void testThatIteratorReturnsBestSolution() {
//		
//		/* check best returned solution */
//		assertNotNull("The algorithm has not returned any solution.", bestSolution);
//		String bestPacking = "";
//		for (int i = 0; i < 10; i++) {
//			if (bestSolution.getPackedObjects().contains(String.valueOf(i))) {
//				bestPacking += "1";
//			} else {
//				bestPacking += "0";
//			}
//		}
//		logger.info("Best knapsack has the value: {}", bestValue);
//		assertEquals("1111010000", bestPacking);
//	}

	@Override
	public KnapsackProblem getSimpleProblemInputForGeneralTestPurposes() {
		return KnapsackProblemGenerator.getKnapsackProblem(5);
	}

	@Override
	public KnapsackProblem getDifficultProblemInputForGeneralTestPurposes() {
		return KnapsackProblemGenerator.getKnapsackProblem(5000);
	}

	@Override
	public Map<KnapsackProblem, Collection<Set<String>>> getProblemsWithSolutions() {
		Map<KnapsackProblem, Collection<Set<String>>> problemsWithSolutions = new HashMap<>();
		EnumeratingKnapsackSolver solver = new EnumeratingKnapsackSolver();
		for (int n = 2; n <= 8; n++) {
			KnapsackProblem kp = KnapsackProblemGenerator.getKnapsackProblem(n, 0);
			try {
				problemsWithSolutions.put(kp, solver.getSolutions(kp));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		return problemsWithSolutions;
	}
}

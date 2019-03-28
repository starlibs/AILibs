package jaicore.testproblems.knapsack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class KnapsackProblemGenerator {
	public static KnapsackProblem getKnapsackProblem(int numObjects) {
		return getKnapsackProblem(numObjects, 0);
	}
	
	public static KnapsackProblem getKnapsackProblem(int numObjects, int seed) {
		
		/* create knapsack problem */
		Random r = new Random(seed);
		Set<String> objects = new HashSet<String>();
		Map<String, Double> weights = new HashMap<>();
		Map<String, Double> values = new HashMap<>();
		Map<Set<String>, Double> bonusPoints;
		for (int i = 0; i < numObjects; i++) {
			objects.add(String.valueOf(i));
		}
		for (int i = 0; i < numObjects; i++)
			weights.put("" + i, r.nextInt(100) * 1.0);
		for (int i = 0; i < numObjects; i++)
			values.put("" + i, r.nextInt(100) * 1.0);
		
		bonusPoints = new HashMap<>();
		Set<String> bonusCombination = new HashSet<>();
		bonusCombination.add("0");
		bonusCombination.add("2");
		bonusPoints.put(bonusCombination, 25.0d);
		KnapsackProblem kp = new KnapsackProblem(objects, values, weights, bonusPoints, numObjects * 20);
		return kp;
	}
}

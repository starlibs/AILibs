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
		Set<String> objects = new HashSet<>();
		Map<String, Double> weights = new HashMap<>();
		Map<String, Double> values = new HashMap<>();
		Map<Set<String>, Double> bonusPoints;
		for (int i = 0; i < numObjects; i++) {
			objects.add(String.valueOf(i));
		}
		double minWeight = 100;
		for (int i = 0; i < numObjects; i++) {
			double weight = r.nextInt(100) * 1.0;
			weights.put("" + i, weight);
			if (weight < minWeight) {
				minWeight = weight;
			}
		}
		for (int i = 0; i < numObjects; i++)
			values.put("" + i, r.nextInt(100) * 1.0);
		
		bonusPoints = new HashMap<>();
		Set<String> bonusCombination = new HashSet<>();
		bonusCombination.add("0");
		bonusCombination.add("2");
		bonusPoints.put(bonusCombination, 25.0d);
		return new KnapsackProblem(objects, values, weights, bonusPoints, Math.max(numObjects * 20.0, minWeight));
	}
}

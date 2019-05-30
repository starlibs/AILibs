package jaicore.search.evaluationproblems;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.interfaces.IPathUnification;
import jaicore.search.algorithms.standard.bestfirst.RandomCompletionEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.algorithms.standard.core.ParentDiscarding;
import jaicore.search.evaluationproblems.KnapsackProblem.KnapsackNode;
import jaicore.search.structure.core.Node;

public class KnapsackOptimizationExample {
	
	public static void main (String...args) {
		KnapsackOptimizationExample example = new KnapsackOptimizationExample();
		try {
			example.testKnapsackProblem();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private Set<String> objects;
	private Map<String, Double> weights;
	private Map<String, Double> values;
	private Map<Set<String>, Double> bonusPoints;
	
	public void testKnapsackProblem() throws InterruptedException {
		objects = new HashSet<String>();
		for (int i = 0; i < 10; i++) {
			objects.add(String.valueOf(i));
		}
		weights = new HashMap<>();
		weights.put("0", 23.0d);
		weights.put("1", 31.0d);
		weights.put("2", 29.0d);
		weights.put("3", 44.0d);
		weights.put("4", 53.0d);
		weights.put("5", 38.0d);
		weights.put("6", 63.0d);
		weights.put("7", 85.0d);
		weights.put("8", 89.0d);
		weights.put("9", 82.0d);
		values = new HashMap<>();
		values.put("0", 92.0d);
		values.put("1", 57.0d);
		values.put("2", 49.0d);
		values.put("3", 68.0d);
		values.put("4", 60.0d);
		values.put("5", 43.0d);
		values.put("6", 67.0d);
		values.put("7", 84.0d);
		values.put("8", 87.0d);
		values.put("9", 72.0d);
		bonusPoints = new HashMap<>();
		Set<String> bonusCombination = new HashSet<>();
		bonusCombination.add("0");
		bonusCombination.add("2");
		bonusPoints.put(bonusCombination, 25.0d);
		KnapsackProblem knapsackProblem = new KnapsackProblem(objects, values, weights, bonusPoints, 165);
		ORGraphSearch<KnapsackNode, String, Double> search = new ORGraphSearch<>(
                knapsackProblem.getGraphGenerator(),
                new RandomCompletionEvaluator<>(
                        new Random(123l),
                        3,
                        new IPathUnification<KnapsackNode>() {

                            @Override
                            public List<KnapsackNode> getSubsumingKnownPathCompletion(
                                    Map<List<KnapsackNode>, List<KnapsackNode>> knownPathCompletions, List<KnapsackNode> path)
                                    throws InterruptedException {
                                return null;
                            }
                        },
                        knapsackProblem.getSolutionEvaluator()
                ),
                ParentDiscarding.ALL
        );
		
		SimpleGraphVisualizationWindow<Node<KnapsackNode, Double>> win = new SimpleGraphVisualizationWindow<>(search);
		win.getPanel().setTooltipGenerator(n->n.getPoint().toString());

		KnapsackNode bestSolution = null;
		double bestValue = 0.0d;
		
		while(search.hasNext()) {
			List<KnapsackNode> solution = search.nextSolution();
			if (solution != null) {
				double value = getValueOfKnapsack(solution.get(solution.size() - 1));
				if (value > bestValue) {
					bestSolution = solution.get(solution.size() -1);
					bestValue = value;
				}
			} else {
				break;
			}
		}
		String bestPacking = "";
		for (int i = 0; i < 10; i++) {
			if (bestSolution.getPackedObjects().contains(String.valueOf(i))) {
				bestPacking += "1";
			} else {
				bestPacking += "0";
			}
		}
		System.out.println("Best knapsack has the value: " + bestValue);
		assertEquals(bestPacking, "1111010000");
	}
	
	private double getValueOfKnapsack(KnapsackNode knapsack) {
		if (knapsack == null || knapsack.getPackedObjects() == null || knapsack.getPackedObjects().size() == 0) {
			return 0.0d;
		} else {
			double value = 0.0d;
			for (String object: knapsack.getPackedObjects()) {
				value += values.get(object);
			}
			for (Set<String> bonusCombination : bonusPoints.keySet()) {
				boolean allContained = true;
				for (String object : bonusCombination ) {
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
	
}

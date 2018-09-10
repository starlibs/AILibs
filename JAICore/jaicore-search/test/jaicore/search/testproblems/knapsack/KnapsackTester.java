package jaicore.search.testproblems.knapsack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.graph.IGraphAlgorithmListener;
import jaicore.search.algorithms.standard.ORGraphSearchTester;
import jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.testproblems.knapsack.KnapsackProblem.KnapsackNode;

public abstract class KnapsackTester<I, O, VSearch,ESearch> extends ORGraphSearchTester<KnapsackProblem, I, O, KnapsackNode, String, Double, VSearch, ESearch> implements IGraphAlgorithmListener<VSearch, ESearch> {

	
	private Map<String, Double> weights;
	private Map<String, Double> values;
	private Map<Set<String>, Double> bonusPoints;

	private KnapsackProblem getKnapsackProblem(int numObjects) {
		/* create knapsack problem */
		Random r = new Random(0);
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
	
	public IGraphSearch<I, O, KnapsackNode, String, Double, VSearch, ESearch> getSearch() {
		
		/* create knapsack problem */
		Set<String> objects = new HashSet<String>();
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
		KnapsackProblem kp = new KnapsackProblem(objects, values, weights, bonusPoints, 165);
		
		/* reduce knapsack problem to graph search problem */
		searchFactory.setProblemInput(getProblemReducer().transform(kp));
		return searchFactory.getAlgorithm();
	}

	public double getValueOfKnapsack(KnapsackNode knapsack) {
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
	
	IGraphSearchFactory<I, O, KnapsackNode, String, Double, VSearch, ESearch> searchFactory = getFactory();

	@Override
	public void testThatAnEventForEachPossibleSolutionIsEmittedInSimpleCall() throws Throwable {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void testThatAnEventForEachPossibleSolutionIsEmittedInParallelizedCall() throws Throwable {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void testThatIteratorReturnsEachPossibleSolution() throws Throwable {

	}
	
	@Test
	public void testThatIteratorReturnsBestSolution() {

		KnapsackNode bestSolution = null;
		double bestValue = 0.0d;
		IGraphSearch<I, O, KnapsackNode, String, Double, VSearch, ESearch> search = getSearch();
//		new SimpleGraphVisualizationWindow<>(search);
		Iterator<AlgorithmEvent> iterator = search.iterator();
		assertNotNull("The search algorithm does return NULL as an iterator for itself.", iterator);
		boolean initialized = false;
		boolean terminated = false;
		int solutions = 0;
		while (iterator.hasNext()) {
			AlgorithmEvent e = search.next();
			assertNotNull("The search iterator has returned NULL even though hasNext suggested that more event should come.", e);
			if (!initialized) {
				assertTrue(e instanceof AlgorithmInitializedEvent);
				initialized = true;
			} else if (e instanceof AlgorithmFinishedEvent) {
				terminated = true;
			} else {
				assertTrue(!terminated);
				if (e instanceof EvaluatedSearchSolutionCandidateFoundEvent) {
					solutions++;
					List<KnapsackNode> solution = ((EvaluatedSearchSolutionCandidateFoundEvent<KnapsackNode,String,Double>) e).getSolutionCandidate().getNodes();
					double value = getValueOfKnapsack(solution.get(solution.size() - 1));
					System.out.println("New solution with value "+ value);
					if (value > bestValue) {
						bestSolution = solution.get(solution.size() - 1);
						bestValue = value;
					}
					
//					System.out.print("\n\t");
//					solution.forEach(node -> System.out.print( node.getCurLocation() + "-"));
				}
			}
		}
		
		/* check best returned solution */
		assertNotNull("The algorithm has not returned any solution.", bestSolution);
		String bestPacking = "";
		for (int i = 0; i < 10; i++) {
			if (bestSolution.getPackedObjects().contains(String.valueOf(i))) {
				bestPacking += "1";
			} else {
				bestPacking += "0";
			}
		}
		System.out.println("Best knapsack has the value: " + bestValue);
		assertEquals("1111010000", bestPacking);
	}

	@Override
	public I getSimpleProblemInputForGeneralTestPurposes() {
		return getProblemReducer().transform(getKnapsackProblem(5));
	}

	@Override
	public I getDifficultProblemInputForGeneralTestPurposes() {
		return getProblemReducer().transform(getKnapsackProblem(5000));
	}
}

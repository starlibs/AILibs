package jaicore.search.testproblems.enhancedttsp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.util.MathUtils;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.SolutionCandidateFoundEvent;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.graph.IGraphAlgorithmListener;
import jaicore.graph.LabeledGraph;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.ORGraphSearchTester;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;

public abstract class EnhancedTTSPTester<I, O, VSearch, ESearch> extends ORGraphSearchTester<EnhancedTTSP, I, O, EnhancedTTSPNode, String, Double, VSearch, ESearch>
		implements IGraphAlgorithmListener<VSearch, ESearch> {

	private static final int MAX_N = 8;
	private static final int MAX_DISTANCE = 12;
	private static final int TIMEOUT_IN_MS = 5 * 60 * 1000;
	private static final boolean VISUALIZE = false;

	

	IGraphSearchFactory<I, O, EnhancedTTSPNode, String, Double, VSearch, ESearch> searchFactory = getFactory();

	private I getSearchProblem(int n) {

		/* create TTSP problem */
		Random r = new Random(0);
		List<Boolean> blockedHours = Arrays.asList(
				new Boolean[] { true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true });
		LabeledGraph<Short, Double> travelGraph;
		travelGraph = new LabeledGraph<>();
		List<Pair<Double, Double>> coordinates = new ArrayList<>();
		for (short i = 0; i < n; i++) {
			coordinates.add(new Pair<>(r.nextDouble() * MAX_DISTANCE, r.nextDouble() * MAX_DISTANCE));
			travelGraph.addItem(i);
		}
		for (short i = 0; i < n; i++) {
			double x1 = coordinates.get(i).getX();
			double y1 = coordinates.get(i).getY();
			for (short j = 0; j < i; j++) { // we assume a symmetric travel graph
				double x2 = coordinates.get(j).getX();
				double y2 = coordinates.get(j).getY();
				double minTravelTime = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
				// System.out.println("(" + x1 + ", " + y1 + ") to (" +x2 +", " + y2 +") = "
				// +minTravelTime);
				travelGraph.addEdge(i, j, minTravelTime);
				travelGraph.addEdge(j, i, minTravelTime);
			}
		}
		EnhancedTTSP ttsp = new EnhancedTTSP(travelGraph, (short) 0, blockedHours, 8, 4.5, 1, 10);

		/* reduce problem to graph search */
		return getProblemReducer().transform(ttsp);
	}
	
	private IGraphSearch<I, O, EnhancedTTSPNode, String, Double, VSearch, ESearch> getSearchAlgorithmForProblem(int n) {
		searchFactory.setProblemInput(getSearchProblem(n));
		return searchFactory.getAlgorithm();
	}

	@Override
	public void testThatIteratorReturnsEachPossibleSolution() throws Throwable {
		for (int n = 3; n <= MAX_N; n++) {
			System.out.print("Checking n = " + n + " ");

			IGraphSearch<I, O, EnhancedTTSPNode, String, Double, VSearch, ESearch> search = getSearchAlgorithmForProblem(n);
			if (VISUALIZE)
				new SimpleGraphVisualizationWindow<>(search);
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
					if (e instanceof SolutionCandidateFoundEvent) {
						solutions++;
						EvaluatedSearchGraphPath<EnhancedTTSPNode, String, Double> solution = (EvaluatedSearchGraphPath)(((SolutionCandidateFoundEvent) e).getSolutionCandidate());
						 List<EnhancedTTSPNode> solutionPath = solution.getNodes();
						 System.out.print("\n\t");
						 solutionPath.forEach(node -> System.out.print( node.getCurLocation() + "-"));
					}
				}
			}
			checkNumberOfSolutions(n,solutions);
			
			System.out.println("done");
		}
	}
	
	private void checkNumberOfSolutions(int n, int givenSolutions) {
		int expected = (int)MathUtils.factorial(n - 1);
		assertEquals("Wrong number of returned solutions for " + n + "-TTSP. Expected " + expected + " and received " + givenSolutions,expected, givenSolutions);
	}
	
	@Override
	public void testThatAnEventForEachPossibleSolutionIsEmittedInSimpleCall() throws Throwable {
		
	}

	@Override
	public void testThatAnEventForEachPossibleSolutionIsEmittedInParallelizedCall() throws Throwable {
		
	}

	@Override
	public I getSimpleProblemInputForGeneralTestPurposes() {
		return getSearchProblem(4);
	}
	
	@Override
	public I getDifficultProblemInputForGeneralTestPurposes() {
		return getSearchProblem(20);
	}
}

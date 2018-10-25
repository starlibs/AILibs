package jaicore.search.testproblems.nqueens;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.eventbus.Subscribe;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.SolutionCandidateFoundEvent;
import jaicore.graph.IGraphAlgorithmListener;
import jaicore.graphvisualizer.gui.VisualizationWindow;
import jaicore.search.algorithms.standard.ORGraphSearchTester;
import jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.core.interfaces.IGraphSearchFactory;

public abstract class NQueenTester<I, O, VSearch, ESearch> extends ORGraphSearchTester<Integer, I, O, QueenNode, String, Double, VSearch, ESearch>
		implements IGraphAlgorithmListener<VSearch, ESearch> {

	int[] numbersOfSolutions = { 2, 10, 4, 40, 92, 352, 724 };

	private AtomicInteger seenSolutions = new AtomicInteger(0);
	private boolean showGraphs = false;

	IGraphSearchFactory<I, O, QueenNode, String, Double, VSearch, ESearch> searchFactory = getFactory();

	private IGraphSearch<I, O, QueenNode, String, Double, VSearch, ESearch> getSearchProblemInput(int n) {
		searchFactory.setProblemInput(n, getProblemReducer());
		IGraphSearch<I, O, QueenNode, String, Double, VSearch, ESearch> search = searchFactory.getAlgorithm();
		return search;
	}

	@Override
	public void testThatIteratorReturnsEachPossibleSolution() {
		if (searchFactory == null)
			throw new IllegalArgumentException("Search factory has not been set");
		for (int i = 0; i < numbersOfSolutions.length; i++) {
			int n = i + 4;
			System.out.print("Checking " + n + "-Queens Problem ... ");
			IGraphSearch<I, O, QueenNode, String, Double, VSearch, ESearch> search = getSearchProblemInput(n);
			assertNotNull("The factory has not returned any search object.", search);
			if (showGraphs)
				new VisualizationWindow<>(search);
			boolean initialized = false;
			boolean terminated = false;
			int solutions = 0;
			Iterator<AlgorithmEvent> iterator = search.iterator();
			assertNotNull("The search algorithm does return NULL as an iterator for itself.", iterator);
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
					if (e instanceof SolutionCandidateFoundEvent)
						solutions++;
				}
			}
			assertEquals("Failed to solve " + n + "-queens problem. Only found " + solutions + "/" + numbersOfSolutions[i] + " solutions.", numbersOfSolutions[i], solutions);
			System.out.println("done");
		}
	}

	@Override
	public void testThatAnEventForEachPossibleSolutionIsEmittedInSimpleCall() throws Exception {
		if (searchFactory == null)
			throw new IllegalArgumentException("Search factory has not been set");
		for (int i = 0; i < numbersOfSolutions.length; i++) {
			int n = i + 4;
			System.out.print("Checking " + n + "-Queens Problem ... ");
			IGraphSearch<I, O, QueenNode, String, Double, VSearch, ESearch> search = getSearchProblemInput(n);
			assertNotNull("The factory has not returned any search object.", search);
			if (showGraphs)
				new VisualizationWindow<>(search);
			search.registerListener(this);
			seenSolutions = new AtomicInteger(0);
			search.call();
			assertEquals(numbersOfSolutions[i], seenSolutions.get());
			System.out.println("done");
		}
	}

	@Override
	public void testThatAnEventForEachPossibleSolutionIsEmittedInParallelizedCall() throws Exception {
		if (searchFactory == null)
			throw new IllegalArgumentException("Search factory has not been set");
		for (int i = 0; i < numbersOfSolutions.length; i++) {
			int n = i + 4;
			System.out.print("Checking " + n + "-Queens Problem ... ");
			IGraphSearch<I, O, QueenNode, String, Double, VSearch, ESearch> search = getSearchProblemInput(n);
			assertNotNull("The factory has not returned any search object.", search);
			if (showGraphs)
				new VisualizationWindow<>(search);
			search.registerListener(this);
			search.setNumCPUs(Runtime.getRuntime().availableProcessors());
			seenSolutions = new AtomicInteger(0);
			search.call();
			assertEquals(numbersOfSolutions[i], seenSolutions.get());
			System.out.println("done");
		}
	}

	@Subscribe
	public void registerSolution(GraphSearchSolutionCandidateFoundEvent<QueenNode, String> solution) {
		seenSolutions.incrementAndGet();
	}

	public boolean isShowGraphs() {
		return showGraphs;
	}

	public void setShowGraphs(boolean showGraphs) {
		this.showGraphs = showGraphs;
	}

	@Override
	public I getSimpleProblemInputForGeneralTestPurposes() {
		return getProblemReducer().transform(4);
	}

	@Override
	public I getDifficultProblemInputForGeneralTestPurposes() {
		return getProblemReducer().transform(100);
	}
}

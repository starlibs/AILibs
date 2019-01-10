package jaicore.search.testproblems.nqueens;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.eventbus.Subscribe;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.events.SolutionCandidateFoundEvent;
import jaicore.graphvisualizer.gui.VisualizationWindow;
import jaicore.search.algorithms.standard.ORGraphSearchTester;
import jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchInput;

public abstract class NQueenTester<I extends GraphSearchInput<QueenNode, String>, O extends SearchGraphPath<QueenNode, String>, VSearch, ESearch> extends ORGraphSearchTester<Integer, I, O, QueenNode, String, VSearch, ESearch> {

//	int[] numbersOfSolutions = { 2, 10, 4, 40, 92, 352, 724 };
	int[] numbersOfSolutions = { 2, 10, 4, 40, 92 };

	private AtomicInteger seenSolutions = new AtomicInteger(0);
	private boolean showGraphs = true;

	IGraphSearchFactory<I, O, QueenNode, String, VSearch, ESearch> searchFactory = getFactory();

	private IGraphSearch<I, O, QueenNode, String, VSearch, ESearch> getSearchProblemInput(int n) {
		searchFactory.setProblemInput(n, getProblemReducer());
		IGraphSearch<I, O, QueenNode, String, VSearch, ESearch> search = searchFactory.getAlgorithm();
		return search;
	}

	@Override
	public void testThatIteratorReturnsEachPossibleSolution() {
		if (searchFactory == null)
			throw new IllegalArgumentException("Search factory has not been set");
		for (int i = 0; i < numbersOfSolutions.length; i++) {
			int n = i + 4;
			System.out.print("Checking " + n + "-Queens Problem ... ");
			IGraphSearch<I, O, QueenNode, String, VSearch, ESearch> search = getSearchProblemInput(n);
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
			IGraphSearch<I, O, QueenNode, String, VSearch, ESearch> search = getSearchProblemInput(n);
			assertNotNull("The factory has not returned any search object.", search);
//			if (showGraphs)
//				new VisualizationWindow<>(search);
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
			IGraphSearch<I, O, QueenNode, String, VSearch, ESearch> search = getSearchProblemInput(n);
			assertNotNull("The factory has not returned any search object.", search);
//			if (showGraphs)
//				new VisualizationWindow<>(search);
			search.registerListener(this);
			search.setNumCPUs(Runtime.getRuntime().availableProcessors());
			seenSolutions = new AtomicInteger(0);
			search.call();
			assertEquals(numbersOfSolutions[i], seenSolutions.get());
			System.out.println("done");
		}
	}

	@Subscribe
	public void registerSolution(GraphSearchSolutionCandidateFoundEvent<QueenNode, String, O> solution) {
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

package jaicore.search.testproblems.nqueens;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.events.SolutionCandidateFoundEvent;
import jaicore.search.algorithms.GraphSearchTester;
import jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import jaicore.search.core.interfaces.AAnyPathInORGraphSearch;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchInput;

public abstract class NQueenTester<I extends GraphSearchInput<QueenNode, String>, O extends SearchGraphPath<QueenNode, String>> extends GraphSearchTester<Integer, I, O, QueenNode, String> {

	private static final String LOGGER_NAME = "testedalgorithm";
	private static final String LOG_MESSAGE = "Checking {}-Queens Problem ... ";
	private static final String ERROR_NO_SEARCH_PRODUCED = "The factory has not returned any search object.";
	private static final String ERROR_FACTORY_NOT_SET = "Search factory has not been set";
	int[] numbersOfSolutions = { 2, 10, 4, 40}; // further numbers of solutions are 92, 352, 724

	private static final Logger logger = LoggerFactory.getLogger(NQueenTester.class);	
	
	private AtomicInteger seenSolutions = new AtomicInteger(0);
	private boolean showGraphs = false;

	IGraphSearchFactory<I, O, QueenNode, String> searchFactory = getFactory();

	private IGraphSearch<I, O, QueenNode, String> getSearchProblemInput(int n) {
		searchFactory.setProblemInput(n, getProblemReducer());
		return searchFactory.getAlgorithm();
	}
	
	@Override
	public void testThatIteratorReturnsEachPossibleSolution() {
		if (searchFactory == null)
			throw new IllegalArgumentException(ERROR_FACTORY_NOT_SET);
		for (int i = 0; i < numbersOfSolutions.length; i++) {
			int n = i + 4;
			logger.info(LOG_MESSAGE, n);
			IGraphSearch<I, O, QueenNode, String> search = getSearchProblemInput(n);
			assertNotNull(ERROR_NO_SEARCH_PRODUCED, search);
			if (search instanceof ILoggingCustomizable) {
				((ILoggingCustomizable) search).setLoggerName(LOGGER_NAME);
			}
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
		}
	}

	@Override
	public void testThatAnEventForEachPossibleSolutionIsEmittedInSimpleCall() throws Exception {
		if (searchFactory == null)
			throw new IllegalArgumentException(ERROR_FACTORY_NOT_SET);
		for (int i = 0; i < numbersOfSolutions.length; i++) {
			int n = i + 4;
			logger.info(LOG_MESSAGE, n);
			IGraphSearch<I, O, QueenNode, String> search = getSearchProblemInput(n);
			assertNotNull(ERROR_NO_SEARCH_PRODUCED, search);
			if (search instanceof ILoggingCustomizable) {
				((ILoggingCustomizable) search).setLoggerName(LOGGER_NAME);
			}
			search.registerListener(this);
			seenSolutions = new AtomicInteger(0);
			search.call();
			assertEquals(search instanceof AAnyPathInORGraphSearch ? 1 : numbersOfSolutions[i], seenSolutions.get());
		}
	}

	@Override
	public void testThatAnEventForEachPossibleSolutionIsEmittedInParallelizedCall() throws Exception {
		if (searchFactory == null)
			throw new IllegalArgumentException(ERROR_FACTORY_NOT_SET);
		for (int i = 0; i < numbersOfSolutions.length; i++) {
			int n = i + 4;
			logger.info(LOG_MESSAGE, n);
			IGraphSearch<I, O, QueenNode, String> search = getSearchProblemInput(n);
			assertNotNull(ERROR_NO_SEARCH_PRODUCED, search);
			if (search instanceof ILoggingCustomizable) {
				((ILoggingCustomizable) search).setLoggerName(LOGGER_NAME);
			}
			search.registerListener(this);
			search.setNumCPUs(Runtime.getRuntime().availableProcessors());
			seenSolutions = new AtomicInteger(0);
			search.call();
			assertEquals(search instanceof AAnyPathInORGraphSearch ? 1 : numbersOfSolutions[i], seenSolutions.get());

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
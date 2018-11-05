package jaicore.search.testproblems.npuzzle.standard;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.eventbus.Subscribe;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.graph.IGraphAlgorithmListener;
import jaicore.graphvisualizer.gui.VisualizationWindow;
import jaicore.search.algorithms.standard.ORGraphSearchTester;
import jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.core.interfaces.IGraphSearchFactory;

public abstract class NPuzzleStandardTester<I, O, VSearch, ESearch> extends ORGraphSearchTester<NPuzzleProblem, I, O, NPuzzleNode, String, Double, VSearch, ESearch>
		implements IGraphAlgorithmListener<VSearch, ESearch> {

	private final static int SEED = 0;
	private int max_n = 4;
	private int max_solutions = 100;
	private AtomicInteger seenSolutions = new AtomicInteger(0);
	private boolean showGraphs = false;

	private IGraphSearch<I, O, NPuzzleNode, String, Double, VSearch, ESearch> getSearch(int n, int seed) {
		IGraphSearchFactory<I, O, NPuzzleNode, String, Double, VSearch, ESearch> factory = getFactory();
		factory.setProblemInput(new NPuzzleProblem(n, seed), getProblemReducer());
		return factory.getAlgorithm();
	}

	@Override
	public void testThatIteratorReturnsEachPossibleSolution() {
		for (int n = 3; n <= max_n; n++) {
			System.out.print("Checking first 100 solutions of " + n + "-puzzle ... ");
			IGraphSearch<I,O,NPuzzleNode, String, Double, VSearch, ESearch> search = getSearch(n, SEED);
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
					if (e instanceof GraphSearchSolutionCandidateFoundEvent) {
						solutions++;
						@SuppressWarnings("unchecked")
						List<NPuzzleNode> solutionPath = ((EvaluatedSearchSolutionCandidateFoundEvent<NPuzzleNode, String, Double>) e).getSolutionCandidate().getNodes();
						NPuzzleNode finalNode = solutionPath.get(solutionPath.size() - 1);
						assertTrue("Number of wrong tiles in solution " + finalNode.toString() + " is " + finalNode.getNumberOfWrongTiles(), finalNode.getNumberOfWrongTiles() == 0);
						if (solutions >= max_solutions)
							return;
					}
				}

				System.out.println("done");
			}
		}
	}

	@Subscribe
	public void registerSolution(EvaluatedSearchSolutionCandidateFoundEvent<NPuzzleNode, String, Double> solution) {

		seenSolutions.incrementAndGet();
	}

	public boolean isShowGraphs() {
		return showGraphs;
	}

	public void setShowGraphs(boolean showGraphs) {
		this.showGraphs = showGraphs;
	}

	@Override
	public void testThatAnEventForEachPossibleSolutionIsEmittedInSimpleCall() throws Throwable {
		
	}

	@Override
	public void testThatAnEventForEachPossibleSolutionIsEmittedInParallelizedCall() throws Throwable {
		
	}
	


	@Override
	public I getSimpleProblemInputForGeneralTestPurposes() {
		return getProblemReducer().transform(new NPuzzleProblem(4, 0));
	}

	@Override
	public I getDifficultProblemInputForGeneralTestPurposes() {
		return getProblemReducer().transform(new NPuzzleProblem(10, 0));
	}
}

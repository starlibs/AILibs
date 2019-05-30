package jaicore.search.algorithms.standard.rstar;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeoutException;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic;

/**
 * This test deactivates the solution enumerators, because RStar is not a complete algorithm
 *
 * @author fmohr
 *
 */
public class RStarTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N, A> IGraphSearch<?, ?, N, A> getSearchAlgorithm(final GraphSearchInput<N, A> problem) {
		GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic<N, A> transformedInput = new GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic<>(problem.getGraphGenerator(), (n1,n2) -> 1.0, n -> 0.0, (n1, n2) -> 1.0, (n1, n2) -> n1.equals(n2) ? 0.0 : 1.0, new GraphBasedDistantSuccessorGenerator<>(problem.getGraphGenerator(), 0));

		RStarFactory<N, A> factory = new RStarFactory<>();
		factory.setDelta(2);
		factory.setProblemInput(transformedInput);
		return factory.getAlgorithm();
	}

	@Override
	public void testThatAnEventForEachPossibleSolutionIsEmittedInSimpleCall() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		assertTrue(true);
	}

	@Override
	public void testThatAnEventForEachPossibleSolutionIsEmittedInParallelizedCall() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		assertTrue(true);
	}

	@Override
	public void testThatIteratorReturnsEachPossibleSolution() {
		assertTrue(true);
	}

	@Override
	public void testThatIteratorReturnsEachPossibleSolutionWithParallelization() {
		assertTrue(true);
	}
}

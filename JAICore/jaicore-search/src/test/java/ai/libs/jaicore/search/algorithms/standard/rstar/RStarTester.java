package ai.libs.jaicore.search.algorithms.standard.rstar;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeoutException;

import org.api4.java.ai.graphsearch.problem.IPathSearch;
import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;

import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic;

/**
 * This test deactivates the solution enumerators, because RStar is not a complete algorithm
 *
 * @author fmohr
 *
 */
public class RStarTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N, A> IPathSearch<?, ?, N, A> getSearchAlgorithm(final IPathSearchInput<N, A> problem) {
		GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic<N, A> transformedInput = new GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic<>(problem, (n1,n2) -> 1.0, n -> 0.0, (n1, n2) -> 1.0, (n1, n2) -> n1.equals(n2) ? 0.0 : 1.0, new GraphBasedDistantSuccessorGenerator<>(problem, 0));

		RStarFactory<GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic<N, A>, N, A> factory = new RStarFactory<>();
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

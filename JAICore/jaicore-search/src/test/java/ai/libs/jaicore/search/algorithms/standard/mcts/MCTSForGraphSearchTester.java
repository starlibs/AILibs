package ai.libs.jaicore.search.algorithms.standard.mcts;

import java.util.concurrent.TimeoutException;

import org.api4.java.ai.graphsearch.problem.IPathSearch;
import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.basic.algorithm.AlgorithmCreationException;
import ai.libs.jaicore.basic.algorithm.IAlgorithmTestProblemSet;
import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSFactory;
import ai.libs.jaicore.search.model.other.AgnosticPathEvaluator;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.jaicore.test.MediumTest;

@Tag("mcts")
public abstract class MCTSForGraphSearchTester extends GraphSearchSolutionIteratorTester {

	public abstract <N, A> MCTSFactory<N, A, ?> getFactory();

	@Override
	public <N, A> IPathSearch<?, ?, N, A> getSearchAlgorithm(final IPathSearchInput<N, A> problem) {
		GraphSearchWithPathEvaluationsInput<N, A, Double> newProblem = new GraphSearchWithPathEvaluationsInput<>(problem, new AgnosticPathEvaluator<>());
		MCTSFactory<N, A, ?> factory = this.getFactory();
		factory.withTabooExhaustedNodes(true);
		return new MCTSPathSearch<>(newProblem, factory);
	}

	@Override
	@MediumTest
	@ParameterizedTest(name="Single-Thread test for completeness of event emitter on {0}")
	@MethodSource("getProblemSets") // this is the important difference, because MCTS takes longer to produce all paths!
	public void testThatAnEventForEachPossibleSolutionIsEmittedInSimpleCall(final IAlgorithmTestProblemSet<Object> problemSet)
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException, AlgorithmCreationException {
		super.testThatAnEventForEachPossibleSolutionIsEmittedInSimpleCall(problemSet);
	}

	@Override
	@MediumTest
	@ParameterizedTest(name="Multi-Thread test for completeness of event emitter on {0}")
	@MethodSource("getProblemSets") // this is the important difference, because MCTS takes longer to produce all paths!
	public void testThatAnEventForEachPossibleSolutionIsEmittedInParallelizedCall(final IAlgorithmTestProblemSet<Object> problemSet)
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException, AlgorithmCreationException {
		super.testThatAnEventForEachPossibleSolutionIsEmittedInParallelizedCall(problemSet);
	}

	@Override
	@MediumTest
	@ParameterizedTest(name="Single-Thread test for completeness of iterator on {0}")
	@MethodSource("getProblemSets")
	public void testThatIteratorReturnsEachPossibleSolution(final IAlgorithmTestProblemSet<Object> problemSet)
			throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException, AlgorithmCreationException {
		super.testThatIteratorReturnsEachPossibleSolution(problemSet);
	}

	@Override
	@MediumTest
	@ParameterizedTest(name="Multi-Thread test for completeness of iterator on {0}")
	@MethodSource("getProblemSets")
	public void testThatIteratorReturnsEachPossibleSolutionWithParallelization(final IAlgorithmTestProblemSet<Object> problemSet)
			throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException, AlgorithmCreationException {
		super.testThatIteratorReturnsEachPossibleSolutionWithParallelization(problemSet);
	}
}

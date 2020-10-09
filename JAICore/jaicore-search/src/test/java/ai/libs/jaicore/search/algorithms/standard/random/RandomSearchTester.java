package ai.libs.jaicore.search.algorithms.standard.random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.basic.algorithm.IAlgorithmTestProblemSet;
import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
public class RandomSearchTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N, A> RandomSearch<N, A> getSearchAlgorithm(final IPathSearchInput<N, A> problem) {
		return new RandomSearch<>(problem);
	}

	private List<SearchGraphPath<?, ?>> getTourOfSequences(final IAlgorithmTestProblemSet<?> problemSet, final int seed) throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, AlgorithmTestProblemSetCreationException {
		RandomSearch<?, ?> rs = new RandomSearch<>(this.getSimpleGraphSearchProblemInstance(problemSet), seed);
		List<SearchGraphPath<?, ?>> solutions = new ArrayList<>();
		while (rs.hasNext()) {
			IAlgorithmEvent e = rs.nextWithException();
			if (e instanceof GraphSearchSolutionCandidateFoundEvent) {
				SearchGraphPath<?, ?> path = (SearchGraphPath<?, ?>) ((GraphSearchSolutionCandidateFoundEvent) e).getSolutionCandidate();
				solutions.add(path);
			}
		}
		return solutions;
	}

	@ParameterizedTest
	@MethodSource("getProblemSets")
	public void testDifferentSequencesForDifferentSeeds(final IAlgorithmTestProblemSet<?> problemSet) throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, AlgorithmTestProblemSetCreationException {
		int seed1 = 0;
		int seed2 = 4711;
		assertNotEquals(this.getTourOfSequences(problemSet, seed1), this.getTourOfSequences(problemSet, seed2));
	}

	@ParameterizedTest
	@MethodSource("getProblemSets")
	public void testDeterminismForGivenSeed(final IAlgorithmTestProblemSet<?> problem) throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, AlgorithmTestProblemSetCreationException {
		int seed = 4711;
		assertEquals(this.getTourOfSequences(problem, seed), this.getTourOfSequences(problem, seed));
	}
}

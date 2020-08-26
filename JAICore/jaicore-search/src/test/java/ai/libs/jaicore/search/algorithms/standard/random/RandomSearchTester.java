package ai.libs.jaicore.search.algorithms.standard.random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Test;

import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.search.algorithms.GraphSearchSolutionIteratorTester;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.model.other.SearchGraphPath;

public class RandomSearchTester extends GraphSearchSolutionIteratorTester {

	@Override
	public <N, A> RandomSearch<N, A> getSearchAlgorithm(final IPathSearchInput<N, A> problem) {
		return new RandomSearch<>(problem);
	}

	private List<SearchGraphPath<?, ?>> getTourOfSequences(final int seed) throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, AlgorithmTestProblemSetCreationException {
		RandomSearch<?, ?> rs = new RandomSearch<>(this.getSimpleGraphSearchProblemInstance(), seed);
		List<SearchGraphPath<?, ?>> solutions = new ArrayList<>();
		while (rs.hasNext()) {
			IAlgorithmEvent e = rs.nextWithException();
			if (e instanceof GraphSearchSolutionCandidateFoundEvent) {
				SearchGraphPath<?, ?> path = (SearchGraphPath<?, ?>)((GraphSearchSolutionCandidateFoundEvent) e).getSolutionCandidate();
				System.out.println(path.getHead());
				solutions.add(path);
			}
		}
		return solutions;
	}

	@Test
	public void testDifferentSequencesForDifferentSeeds() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, AlgorithmTestProblemSetCreationException {
		int seed1 = 0;
		int seed2 = 4711;
		assertNotEquals(this.getTourOfSequences(seed1), this.getTourOfSequences(seed2));
	}

	@Test
	public void testDeterminismForGivenSeed() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, AlgorithmTestProblemSetCreationException {
		int seed = 4711;
		assertEquals(this.getTourOfSequences(seed), this.getTourOfSequences(seed));
	}
}

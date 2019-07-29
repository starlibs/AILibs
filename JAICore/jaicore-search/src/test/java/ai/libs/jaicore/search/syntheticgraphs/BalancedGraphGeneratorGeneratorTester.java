package ai.libs.jaicore.search.syntheticgraphs;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.jaicore.search.algorithms.standard.dfs.DepthFirstSearch;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.syntheticgraphs.BalancedGraphGeneratorGenerator.N;

@RunWith(Parameterized.class)
public class BalancedGraphGeneratorGeneratorTester {

	private final int branchingFactor;
	private final int depth;

	public BalancedGraphGeneratorGeneratorTester(final int branchingFactor, final int depth) {
		super();
		this.branchingFactor = branchingFactor;
		this.depth = depth;
	}

	// creates the test data
	@Parameters(name = "branchingFactor = {0}, depth = {1}")
	public static Collection<Object[]> data() {

		final int MAX_BF = 4;
		final int MAX_DEPTH = 4;
		int combos = MAX_BF * MAX_DEPTH;

		Object[][] data = new Object[combos][2];
		int i = 0;
		for (int bf = 1; bf <= MAX_BF; bf++) {
			for (int depth = 1; depth <= MAX_DEPTH; depth ++) {
				data[i][0] = bf;
				data[i][1] = depth;
				i++;
			}
		}
		return Arrays.asList(data);
	}

	@Test
	public void testNumberOfSolutionPaths() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		GraphSearchInput<N, Integer> input = new BalanceGraphSearchProblem(this.branchingFactor, this.depth);
		DepthFirstSearch<N, Integer> rs = new DepthFirstSearch<>(input);
		int solutions = 0;
		while (rs.hasNext()) {
			try {
				SearchGraphPath<N, Integer> path = rs.nextSolutionCandidate();
				assertEquals(this.depth, path.getArcs().size());
				solutions++;
			} catch (NoSuchElementException e) {
			}
		}
		assertEquals(solutions, (int)Math.pow(this.branchingFactor, this.depth));
	}
}

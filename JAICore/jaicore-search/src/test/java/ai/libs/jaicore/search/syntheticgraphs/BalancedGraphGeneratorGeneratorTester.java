package ai.libs.jaicore.search.syntheticgraphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

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
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.balanced.BalanceGraphSearchProblem;

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
			for (int depth = 1; depth <= MAX_DEPTH; depth++) {
				data[i][0] = bf;
				data[i][1] = depth;
				i++;
			}
		}
		return Arrays.asList(data);
	}

	@Test
	public void testNumberOfSolutionPaths() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		GraphSearchInput<ITransparentTreeNode, Integer> input = new BalanceGraphSearchProblem(this.branchingFactor, this.depth);
		DepthFirstSearch<ITransparentTreeNode, Integer> dfs = new DepthFirstSearch<>(input);

		int solutions = 0;
		Map<Integer, Set<BigInteger>> idsPerLayer = new HashMap<>();
		while (dfs.hasNext()) {
			try {
				SearchGraphPath<ITransparentTreeNode, Integer> path = dfs.nextSolutionCandidate();
				assertEquals(this.depth, path.getArcs().size());
				for (ITransparentTreeNode n : path.getNodes()) {
					idsPerLayer.computeIfAbsent(n.getDepth(), k -> new HashSet<>()).add(n.getNumberOfLeftRelativesInSameGeneration());
				}
				solutions++;
			} catch (NoSuchElementException e) {
			}
		}

		/* check that all ids per layer have been enumerated */
		for (int d = 0; d < this.depth; d++) {
			long expectedNodesInThisLayer = (long)Math.pow(this.branchingFactor, d);
			Set<BigInteger> idsInLayer = idsPerLayer.get(d);
			for (long i = 0; i < expectedNodesInThisLayer; i++) {
				assertTrue(idsInLayer.contains(BigInteger.valueOf(i)));
			}
		}
		assertEquals((int) Math.pow(this.branchingFactor, this.depth), solutions);
	}
}

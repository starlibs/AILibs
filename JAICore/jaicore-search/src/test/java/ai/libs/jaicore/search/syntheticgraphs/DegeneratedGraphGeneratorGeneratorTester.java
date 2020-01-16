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
import java.util.Random;
import java.util.Set;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.jaicore.search.algorithms.standard.dfs.DepthFirstSearch;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated.DegeneratedGraphSearchProblem;

@RunWith(Parameterized.class)
public class DegeneratedGraphGeneratorGeneratorTester {

	private final int branchingFactor;
	private final int depth;
	private DegeneratedGraphSearchProblem input;

	public DegeneratedGraphGeneratorGeneratorTester(final int branchingFactor, final int depth) {
		super();
		this.branchingFactor = branchingFactor;
		this.depth = depth;
	}

	// creates the test data
	@Parameters(name = "branchingFactor = {0}, depth = {1}")
	public static Collection<Object[]> data() {

		final int MAX_BF = 8;
		final int MAX_DEPTH = 4;
		int combos = MAX_BF * MAX_DEPTH / 2;

		Object[][] data = new Object[combos][2];
		int i = 0;
		for (int bf = 2; bf <= MAX_BF; bf+=2) {
			for (int depth = 1; depth <= MAX_DEPTH; depth ++) {
				data[i][0] = bf;
				data[i][1] = depth;
				i++;
			}
		}
		return Arrays.asList(data);
	}

	@Before
	public void setupTest() {
		int numDeadEnds = this.branchingFactor / 2;
		this.input = new DegeneratedGraphSearchProblem(new Random(0), numDeadEnds, this.branchingFactor, this.depth);
	}

	private int getNumOfNodesInDepth(final int depth) {
		if (depth == 0) {
			return 1;
		}
		int deadEnds = this.branchingFactor / 2;
		return (int)(Math.pow(this.branchingFactor - deadEnds, depth - 1) * this.branchingFactor);
	}

	private int getNumOfSolutionsInDepth(final int depth) {
		int innerNodes = 0;
		int deadEnds = this.branchingFactor / 2;
		for (int k = 0; k < depth; k++) {
			innerNodes += (int)Math.pow(this.branchingFactor  - deadEnds, k);
		}
		int deadEndSolutions = innerNodes * deadEnds;
		return (int)(Math.pow(this.branchingFactor - deadEnds, depth) + deadEndSolutions);
	}

	@Test
	public void testMetrics() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		int leafsPerInnerLevel = this.branchingFactor / 2;
		int offspringsPerInnerLevel = this.branchingFactor - leafsPerInnerLevel;
		int leafsOnLastLevel = this.branchingFactor;

		for (int islandSize = 1; islandSize <= 3; islandSize ++) {
			int expectedMaxLeafs = -1;
			if (islandSize < leafsOnLastLevel) {
				expectedMaxLeafs = 1;
			}
			else if (islandSize < leafsOnLastLevel * offspringsPerInnerLevel + leafsPerInnerLevel) { // try to unify the nodes under the last inner node
				expectedMaxLeafs = leafsOnLastLevel;
			}
			else {
				return;
			}
			assertEquals("Degenerated tree with bf " + this.branchingFactor + " and depth " + this.depth + " and max island size " + islandSize + " should have a maximum of " + expectedMaxLeafs + " leafs per island.", expectedMaxLeafs, this.input.getGraphGenerator().getMaxNumberOfLeafsInEverySubtreeOfMaxLength(BigInteger.valueOf(islandSize)).intValueExact());
		}
	}

	@Test
	public void testNumberOfSolutionPaths() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		DepthFirstSearch<ITransparentTreeNode, Integer> dfs = new DepthFirstSearch<>(this.input);

		/* compute number of totally expected solutions */
		int totalSolutionsExpected = this.getNumOfSolutionsInDepth(this.depth);
		int solutions = 0;
		Map<Integer, Set<BigInteger>> idsPerLayer = new HashMap<>();
		while (dfs.hasNext()) {
			try {
				SearchGraphPath<ITransparentTreeNode, Integer> path = dfs.nextSolutionCandidate();
				assertEquals(totalSolutionsExpected, path.getRoot().getNumberOfLeafsUnderNode().intValue());
				assertEquals(1, path.getHead().getNumberOfLeafsUnderNode().intValue());
				solutions++;
				for (ITransparentTreeNode n : path.getNodes()) {
					idsPerLayer.computeIfAbsent(n.getDepth(), k -> new HashSet<>()).add(n.getNumberOfLeftRelativesInSameGeneration());
				}
			} catch (NoSuchElementException e) {
			}
		}

		/* check that all ids per layer have been enumerated */
		for (int d = 0; d < this.depth; d++) {
			long expectedNodesInThisLayer = this.getNumOfNodesInDepth(d);
			Set<BigInteger> idsInLayer = idsPerLayer.get(d);
			for (long i = 0; i < expectedNodesInThisLayer; i++) {
				assertTrue("Id " + i + " is missing in layer of depth " + d + ". Total expected number of nodes: " + expectedNodesInThisLayer + ". Ids: " + idsInLayer, idsInLayer.contains(BigInteger.valueOf(i)));
			}
		}
		assertEquals(totalSolutionsExpected, solutions);
	}
}

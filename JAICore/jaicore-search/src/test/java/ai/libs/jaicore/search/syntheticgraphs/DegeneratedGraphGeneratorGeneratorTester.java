package ai.libs.jaicore.search.syntheticgraphs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.search.algorithms.standard.dfs.DepthFirstSearch;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated.DegeneratedGraphSearchProblem;

public class DegeneratedGraphGeneratorGeneratorTester {

	public static Stream<Arguments> getTreeSetups() {

		final int MAX_BF = 8;
		final int MAX_DEPTH = 4;

		List<Arguments> data = new ArrayList<>();
		for (int bf = 2; bf <= MAX_BF; bf += 2) {
			for (int depth = 1; depth <= MAX_DEPTH; depth++) {
				data.add(Arguments.of(bf, depth));
			}
		}
		return data.stream();
	}

	public DegeneratedGraphSearchProblem getProblem(final int branchingFactor, final int depth) {
		int numDeadEnds = branchingFactor / 2;
		return new DegeneratedGraphSearchProblem(new Random(0), numDeadEnds, branchingFactor, depth);
	}

	private int getNumOfNodesInDepth(final int branchingFactor, final int depth) {
		if (depth == 0) {
			return 1;
		}
		int deadEnds = branchingFactor / 2;
		return (int) (Math.pow(branchingFactor - deadEnds, depth - 1) * branchingFactor);
	}

	private int getNumOfSolutionsInDepth(final int branchingFactor, final int depth) {
		int innerNodes = 0;
		int deadEnds = branchingFactor / 2;
		for (int k = 0; k < depth; k++) {
			innerNodes += (int) Math.pow(branchingFactor - deadEnds, k);
		}
		int deadEndSolutions = innerNodes * deadEnds;
		return (int) (Math.pow(branchingFactor - deadEnds, depth) + deadEndSolutions);
	}


	@ParameterizedTest(name = "branchingFactor = {0}, depth = {1}")
	@MethodSource("getTreeSetups")
	public void testMetrics(final int branchingFactor, final int depth) throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		int leafsPerInnerLevel = branchingFactor / 2;
		int offspringsPerInnerLevel = branchingFactor - leafsPerInnerLevel;
		int leafsOnLastLevel = branchingFactor;

		for (int islandSize = 1; islandSize <= 3; islandSize++) {
			int expectedMaxLeafs = -1;
			if (islandSize < leafsOnLastLevel) {
				expectedMaxLeafs = 1;
			} else if (islandSize < leafsOnLastLevel * offspringsPerInnerLevel + leafsPerInnerLevel) { // try to unify the nodes under the last inner node
				expectedMaxLeafs = leafsOnLastLevel;
			} else {
				return;
			}
			assertEquals(expectedMaxLeafs, this.getProblem(branchingFactor, depth).getGraphGenerator().getMaxNumberOfLeafsInEverySubtreeOfMaxLength(BigInteger.valueOf(islandSize)).intValueExact(),
					"Degenerated tree with bf " + branchingFactor + " and depth " + depth + " and max island size " + islandSize + " should have a maximum of " + expectedMaxLeafs + " leafs per island.");
		}
	}

	@ParameterizedTest(name = "branchingFactor = {0}, depth = {1}")
	@MethodSource("getTreeSetups")
	public void testNumberOfSolutionPaths(final int branchingFactor, final int depth) throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		DepthFirstSearch<ITransparentTreeNode, Integer> dfs = new DepthFirstSearch<>(this.getProblem(branchingFactor, depth));

		/* compute number of totally expected solutions */
		int totalSolutionsExpected = this.getNumOfSolutionsInDepth(branchingFactor, depth);
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
		for (int d = 0; d < depth; d++) {
			long expectedNodesInThisLayer = this.getNumOfNodesInDepth(branchingFactor, d);
			Set<BigInteger> idsInLayer = idsPerLayer.get(d);
			for (long i = 0; i < expectedNodesInThisLayer; i++) {
				assertTrue(idsInLayer.contains(BigInteger.valueOf(i)), "Id " + i + " is missing in layer of depth " + d + ". Total expected number of nodes: " + expectedNodesInThisLayer + ". Ids: " + idsInLayer);
			}
		}
		assertEquals(totalSolutionsExpected, solutions);
	}
}

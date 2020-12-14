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
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.balanced.BalanceGraphSearchProblem;

public class BalancedGraphGeneratorGeneratorTester {

	public static Stream<Arguments> getTreeConfigurations() {

		final int MAX_BF = 4;
		final int MAX_DEPTH = 4;

		List<Arguments> data = new ArrayList<>();
		for (int bf = 1; bf <= MAX_BF; bf++) {
			for (int depth = 1; depth <= MAX_DEPTH; depth++) {
				data.add(Arguments.of(bf, depth));
			}
		}
		return data.stream();
	}

	@ParameterizedTest(name = "branchingFactor = {0}, depth = {1}")
	@MethodSource("getTreeConfigurations")
	public void testNumberOfSolutionPaths(final int branchingFactor, final int depth) throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		GraphSearchInput<ITransparentTreeNode, Integer> input = new BalanceGraphSearchProblem(branchingFactor, depth);
		DepthFirstSearch<ITransparentTreeNode, Integer> dfs = new DepthFirstSearch<>(input);

		int solutions = 0;
		Map<Integer, Set<BigInteger>> idsPerLayer = new HashMap<>();
		while (dfs.hasNext()) {
			try {
				SearchGraphPath<ITransparentTreeNode, Integer> path = dfs.nextSolutionCandidate();
				assertEquals(depth, path.getArcs().size());
				for (ITransparentTreeNode n : path.getNodes()) {
					idsPerLayer.computeIfAbsent(n.getDepth(), k -> new HashSet<>()).add(n.getNumberOfLeftRelativesInSameGeneration());
				}
				solutions++;
			} catch (NoSuchElementException e) {
			}
		}

		/* check that all ids per layer have been enumerated */
		for (int d = 0; d < depth; d++) {
			long expectedNodesInThisLayer = (long) Math.pow(branchingFactor, d);
			Set<BigInteger> idsInLayer = idsPerLayer.get(d);
			for (long i = 0; i < expectedNodesInThisLayer; i++) {
				assertTrue(idsInLayer.contains(BigInteger.valueOf(i)));
			}
		}
		assertEquals((int) Math.pow(branchingFactor, depth), solutions);
	}
}

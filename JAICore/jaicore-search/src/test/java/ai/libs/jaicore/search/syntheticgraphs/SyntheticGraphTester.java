package ai.libs.jaicore.search.syntheticgraphs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.events.result.ISolutionCandidateFoundEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.basic.Tester;
import ai.libs.jaicore.search.algorithms.standard.dfs.DepthFirstSearch;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;

public abstract class SyntheticGraphTester extends Tester {

	public abstract IPathSearchInput<ITransparentTreeNode, Integer> getSearchProblem(final int branchingFactor, final int depth, final int distanceToIslands, final int numberOfIslandsWithTreasure);

	public abstract IIslandModel getIslandModel(final int branchingFactor, final int depth, final int distanceToIslands, final int numberOfIslandsWithTreasure);

	public abstract int getExpectedNumberOfIslands(final int branchingFactor, final int depth, final int distanceToIslands, final int numberOfIslandsWithTreasure); // we don't allow BigInteger here, because this could not be tested anyway!

	public abstract int getMaximumIslandSizes(final int branchingFactor, final int depth, final int distanceToIslands, final int numberOfIslandsWithTreasure); // we don't allow BigInteger here, because this could not be tested anyway!

	public abstract int getMinimumIslandSizes(final int branchingFactor, final int depth, final int distanceToIslands, final int numberOfIslandsWithTreasure); // we don't allow BigInteger here, because this could not be tested anyway!

	public abstract int getNumberOfTreasureIslands(final int branchingFactor, final int depth, final int distanceToIslands, final int numberOfIslandsWithTreasure); // we don't allow BigInteger here, because this could not be tested anyway!

	public abstract boolean isPathATreasure(final int branchingFactor, final int depth, final int distanceToIslands, final int numberOfIslandsWithTreasure, ILabeledPath<ITransparentTreeNode, Integer> path) throws PathEvaluationException, InterruptedException;

	@ParameterizedTest(name = "branchingFactor = {0}, depth = {1}, distanceToIslands = {2}, numberOfIslandsWithTreasure = {3}")
	@MethodSource("getTreeSetups")
	public void testIslandModel(final int branchingFactor, final int depth, final int distanceToIslands, final int numberOfIslandsWithTreasure) throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, PathEvaluationException {
		int expectedNumberOfIslands = this.getExpectedNumberOfIslands(branchingFactor, depth, distanceToIslands, numberOfIslandsWithTreasure);
		int expectedMinSizeOfAnyIsland = this.getMinimumIslandSizes(branchingFactor, depth, distanceToIslands, numberOfIslandsWithTreasure);
		int expectedMaxSizeOfAnyIsland = this.getMaximumIslandSizes(branchingFactor, depth, distanceToIslands, numberOfIslandsWithTreasure);
		int expectedNumberOfTreasureIslands = this.getNumberOfTreasureIslands(branchingFactor, depth, distanceToIslands, numberOfIslandsWithTreasure);
		this.logger.info("Testing island model. Expectations: {} islands, of size between {} and {}, and {} treasure paths.", expectedNumberOfIslands, expectedMinSizeOfAnyIsland, expectedMaxSizeOfAnyIsland);
		IIslandModel im = this.getIslandModel(branchingFactor, depth, distanceToIslands, numberOfIslandsWithTreasure);
		DepthFirstSearch<ITransparentTreeNode, Integer> rs = new DepthFirstSearch<>(this.getSearchProblem(branchingFactor, depth, distanceToIslands, numberOfIslandsWithTreasure));
		Map<Integer, Integer> islandSizes = new HashMap<>();
		Set<Integer> treasureIslands = new HashSet<>();
		int maxSize = 0;
		int treasureCount = 0;
		while (rs.hasNext()) {
			IAlgorithmEvent e = rs.next();
			if (e instanceof ISolutionCandidateFoundEvent) {
				SearchGraphPath<ITransparentTreeNode, Integer> path = ((ISolutionCandidateFoundEvent<SearchGraphPath<ITransparentTreeNode, Integer>>) e).getSolutionCandidate();
				int island = im.getIsland(path).intValueExact(); // this must not be a big number in this test
				int size = islandSizes.computeIfAbsent(island, i -> 0) + 1;
				maxSize = Math.max(maxSize, size);
				assertTrue(maxSize <= expectedMaxSizeOfAnyIsland, "The maximum size of islands is not correct. There is now an island with size " + size);
				islandSizes.put(island, size);
				if (this.isPathATreasure(branchingFactor, depth, distanceToIslands, numberOfIslandsWithTreasure, path)) {
					treasureIslands.add(island);
					treasureCount++;
					assertFalse(treasureCount > expectedNumberOfTreasureIslands * expectedMaxSizeOfAnyIsland, "The number of expected treasure paths has been exceeed.");
				}
			}
		}
		assertEquals(expectedNumberOfIslands, islandSizes.keySet().size(), "The number of islands is not correct.");
		int minSize = islandSizes.values().stream().min((x, y) -> Integer.compare(x, y)).get();
		assertTrue(minSize >= expectedMinSizeOfAnyIsland, "There is an island of size only " + minSize + " while required minimum is " + expectedMinSizeOfAnyIsland);
		assertEquals(expectedNumberOfTreasureIslands, treasureIslands.size(), "The number of expected treasure islands is not correct.");
		assertTrue(treasureCount >= expectedNumberOfTreasureIslands * expectedMinSizeOfAnyIsland, "The number of expected treasure paths is too low.");
	}
}

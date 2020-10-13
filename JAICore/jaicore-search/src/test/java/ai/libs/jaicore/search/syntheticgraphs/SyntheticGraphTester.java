package ai.libs.jaicore.search.syntheticgraphs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.events.result.ISolutionCandidateFoundEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.search.algorithms.standard.dfs.DepthFirstSearch;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;

public abstract class SyntheticGraphTester extends ATest {

	public void testIslandModel(final ISyntheticTreasureIslandProblem searchProblem) throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, PathEvaluationException {
		int expectedNumberOfIslands = searchProblem.getExpectedNumberOfIslands();
		int expectedMinSizeOfAnyIsland = searchProblem.getMinimumIslandSizes();
		int expectedMaxSizeOfAnyIsland = searchProblem.getMaximumIslandSizes();
		int expectedNumberOfTreasureIslands = searchProblem.getNumberOfTreasureIslands();

		this.logger.info("Testing island model. Expectations: {} islands, of size between {} and {}, and {} treasure paths.", expectedNumberOfIslands, expectedMinSizeOfAnyIsland, expectedMaxSizeOfAnyIsland);
		IIslandModel im = searchProblem.getIslandModel();
		DepthFirstSearch<ITransparentTreeNode, Integer> rs = new DepthFirstSearch<>(searchProblem);
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
				if (searchProblem.isPathATreasure(path)) {
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

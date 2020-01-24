package ai.libs.jaicore.search.syntheticgraphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import org.junit.Test;

import ai.libs.jaicore.basic.Tester;
import ai.libs.jaicore.search.algorithms.standard.dfs.DepthFirstSearch;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;

public abstract class SyntheticGraphTester extends Tester {

	public abstract IPathSearchInput<ITransparentTreeNode, Integer> getSearchProblem();

	public abstract IIslandModel getIslandModel();

	public abstract int getExpectedNumberOfIslands(); // we don't allow BigInteger here, because this could not be tested anyway!

	public abstract int getMaximumIslandSizes(); // we don't allow BigInteger here, because this could not be tested anyway!

	public abstract int getMinimumIslandSizes(); // we don't allow BigInteger here, because this could not be tested anyway!

	public abstract int getNumberOfTreasureIslands(); // we don't allow BigInteger here, because this could not be tested anyway!

	public abstract boolean isPathATreasure(ILabeledPath<ITransparentTreeNode, Integer> path) throws PathEvaluationException, InterruptedException;

	@Test
	public void testIslandModel() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, PathEvaluationException {
		int expectedNumberOfIslands = this.getExpectedNumberOfIslands();
		int expectedMinSizeOfAnyIsland = this.getMinimumIslandSizes();
		int expectedMaxSizeOfAnyIsland = this.getMaximumIslandSizes();
		int expectedNumberOfTreasureIslands = this.getNumberOfTreasureIslands();
		this.logger.info("Testing island model. Expectations: {} islands, of size between {} and {}, and {} treasure paths.", expectedNumberOfIslands, expectedMinSizeOfAnyIsland, expectedMaxSizeOfAnyIsland);
		IIslandModel im = this.getIslandModel();
		DepthFirstSearch<ITransparentTreeNode, Integer> rs = new DepthFirstSearch<>(this.getSearchProblem());
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
				assertTrue("The maximum size of islands is not correct. There is now an island with size " + size, maxSize <= expectedMaxSizeOfAnyIsland);
				islandSizes.put(island, size);
				if (this.isPathATreasure(path)) {
					treasureIslands.add(island);
					treasureCount ++;
					assertFalse("The number of expected treasure paths has been exceeed.", treasureCount > expectedNumberOfTreasureIslands * expectedMaxSizeOfAnyIsland);
				}
			}
		}
		assertEquals("The number of islands is not correct.", expectedNumberOfIslands, islandSizes.keySet().size());
		int minSize = islandSizes.values().stream().min((x, y) -> Integer.compare(x, y)).get();
		assertTrue("There is an island of size only " + minSize + " while required minimum is " + expectedMinSizeOfAnyIsland, minSize >= expectedMinSizeOfAnyIsland);
		assertEquals("The number of expected treasure islands is not correct.", expectedNumberOfTreasureIslands, treasureIslands.size());
		assertTrue("The number of expected treasure paths is too low.", treasureCount >= expectedNumberOfTreasureIslands * expectedMinSizeOfAnyIsland);
	}
}

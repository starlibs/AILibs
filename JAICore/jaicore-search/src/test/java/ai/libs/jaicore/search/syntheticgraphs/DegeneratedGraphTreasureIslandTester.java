package ai.libs.jaicore.search.syntheticgraphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.jaicore.search.algorithms.standard.dfs.DepthFirstSearch;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated.DegeneratedGraphSearchWithPathEvaluationsProblem;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.equalsized.EqualSizedIslandsModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.noisymean.ChaoticMeansTreasureModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.noisymean.NoisyMeanTreasureModel;

@RunWith(Parameterized.class)
public class DegeneratedGraphTreasureIslandTester {

	private final int branchingFactor;
	private final int depth;
	private final int islandSize;
	private final int numberOfIslandsWithTreasure;

	public DegeneratedGraphTreasureIslandTester(final int branchingFactor, final int depth, final int islandSize, final int numberOfIslandsWithTreasure) {
		super();
		this.branchingFactor = branchingFactor;
		this.depth = depth;
		this.islandSize = islandSize;
		this.numberOfIslandsWithTreasure = numberOfIslandsWithTreasure;
	}

	// creates the test data
	@Parameters(name = "branchingFactor = {0}, depth = {1}, islandSize = {2}, numberOfIslandsWithTreasure = {3}")
	public static Collection<Object[]> data() {

		final int MAX_BF = 8;
		final int MAX_DEPTH = 4;
		final int MAX_ISLANDSIZE = 10;
		int combos = (MAX_BF / 2) * MAX_DEPTH * MAX_ISLANDSIZE;

		Object[][] data = new Object[combos][4];
		int i = 0;
		for (int bf = 2; bf <= MAX_BF; bf+=2) {
			for (int depth = 1; depth <= MAX_DEPTH; depth++) {
				for (int islandSize = 1; islandSize <= MAX_ISLANDSIZE; islandSize++) {
					data[i][0] = bf;
					data[i][1] = depth;
					data[i][2] = (int)Math.min(Math.pow(bf / 2, depth), islandSize);
					data[i][3] = 1;
					i++;
				}
			}
		}
		return Arrays.asList(data);
	}

	@Test
	public void testIslandSizes() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, PathEvaluationException {
		IIslandModel model = new EqualSizedIslandsModel(this.islandSize);
		ChaoticMeansTreasureModel gen = new ChaoticMeansTreasureModel(this.numberOfIslandsWithTreasure, model, 0);
		DegeneratedGraphSearchWithPathEvaluationsProblem input = new DegeneratedGraphSearchWithPathEvaluationsProblem(new Random(0), this.branchingFactor / 2,this.branchingFactor, this.depth, gen);
		NoisyMeanTreasureModel evaluator = (NoisyMeanTreasureModel)input.getPathEvaluator();
		DepthFirstSearch<ITransparentTreeNode, Integer> rs = new DepthFirstSearch<>(input);
		Map<Long, Long> islandSizes = new HashMap<>();
		while (rs.hasNext()) {
			try {
				SearchGraphPath<ITransparentTreeNode, Integer> path = rs.nextSolutionCandidate();
				double score = evaluator.evaluate(path);
				assertEquals(score, evaluator.evaluate(path), 0.000001);
				long island = gen.getIslandModel().getIsland(path);
				long counter = islandSizes.computeIfAbsent(island, i -> (long)0);
				assertTrue("Island " + island + " already has " + counter + " path(s). Cannot add another one.", counter < this.islandSize);
				islandSizes.put(island, counter + 1);
			} catch (NoSuchElementException e) {
			}
		}

		/* check root*/
		ITransparentTreeNode rootNode = input.getGraphGenerator().getRootGenerator().getRoots().iterator().next();
		assertEquals((int)Math.ceil((1.0 * rootNode.getNumberOfLeafsUnderNode()) / this.islandSize), model.getNumberOfIslands()); // we expect that there should be a constant number of islands
	}

	@Test
	public void testNumberOfTreasurePaths() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, PathEvaluationException {
		System.out.println("Island size: " + this.islandSize);
		IIslandModel model = new EqualSizedIslandsModel(this.islandSize);
		ChaoticMeansTreasureModel gen = new ChaoticMeansTreasureModel(this.numberOfIslandsWithTreasure, model, 0);
		DegeneratedGraphSearchWithPathEvaluationsProblem input = new DegeneratedGraphSearchWithPathEvaluationsProblem(new Random(0), this.branchingFactor / 2,this.branchingFactor, this.depth, gen);
		NoisyMeanTreasureModel evaluator = (NoisyMeanTreasureModel)input.getPathEvaluator();
		DepthFirstSearch<ITransparentTreeNode, Integer> rs = new DepthFirstSearch<>(input);
		int numberOfTreasureSolutions = 0;
		while (rs.hasNext()) {
			try {
				SearchGraphPath<ITransparentTreeNode, Integer> path = rs.nextSolutionCandidate();
				double score = evaluator.evaluate(path);
				assertEquals(score, evaluator.evaluate(path), 0.000001);
				long island = gen.getIslandModel().getIsland(path);
				boolean shouldBeATreasure = gen.isPathToTreasureIsland(path);
				assertTrue("Path " + path.getArcs() + " with score " + score + " and associated island " + island + " is false positive or false negative. Should be a treasure is set to " + shouldBeATreasure, shouldBeATreasure == (score < 10));
				if (score < 10) {
					numberOfTreasureSolutions++;
				}
			} catch (NoSuchElementException e) {
			}
		}
		int expectedNumberOfTreasureSolutions = this.islandSize * this.numberOfIslandsWithTreasure;
		assertEquals(expectedNumberOfTreasureSolutions, numberOfTreasureSolutions);
	}
}

package ai.libs.jaicore.search.syntheticgraphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.jaicore.search.algorithms.standard.random.RandomSearch;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.jaicore.search.syntheticgraphs.BalancedGraphGeneratorGenerator.N;

@RunWith(Parameterized.class)
public class BalancedTreasureIslandTester {

	private final int branchingFactor;
	private final int depth;
	private final int distanceToIslands;
	private final int numberOfIslandsWithTreasure;

	public BalancedTreasureIslandTester(final int branchingFactor, final int depth, final int distanceToIslands, final int numberOfIslandsWithTreasure) {
		super();
		this.branchingFactor = branchingFactor;
		this.depth = depth;
		this.distanceToIslands = distanceToIslands;
		this.numberOfIslandsWithTreasure = numberOfIslandsWithTreasure;
	}

	// creates the test data
	@Parameters(name = "branchingFactor = {0}, depth = {1}, distanceToIslands = {2}, numberOfIslandsWithTreasure = {3}")
	public static Collection<Object[]> data() {

		final int MAX_BF = 4;
		final int MAX_DEPTH = 4;
		int combos = MAX_BF * MAX_DEPTH;

		Object[][] data = new Object[combos][4];
		int i = 0;
		for (int bf = 1; bf <= MAX_BF; bf++) {
			for (int depth = 1; depth <= MAX_DEPTH; depth++) {
				data[i][0] = bf;
				data[i][1] = depth;
				data[i][2] = Math.min(3, depth);
				data[i][3] = Math.min(2, (int)Math.pow(bf, (int)data[i][2] - 1));
				i++;
			}
		}
		return Arrays.asList(data);
	}

	@Test
	public void testNumberOfTreasurePaths() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, PathEvaluationException {
		BalanceGraphSearchProblem plainInput = new BalanceGraphSearchProblem(this.branchingFactor, this.depth);
		TreasureIslandPathCostGenerator evaluator = new TreasureIslandPathCostGenerator(this.numberOfIslandsWithTreasure, this.distanceToIslands, (int)Math.pow(this.branchingFactor, this.distanceToIslands));
		GraphSearchWithPathEvaluationsInput<N, Integer, Double> input = new GraphSearchWithPathEvaluationsInput<>(plainInput, evaluator);
		RandomSearch<N, Integer> rs = new RandomSearch<>(input);
		int numberOfTreasureSolutions = 0;
		while (rs.hasNext()) {
			try {
				SearchGraphPath<N, Integer> path = rs.nextSolutionCandidate();
				assertEquals(this.depth, path.getArcs().size());
				double score = evaluator.evaluate(path);
				N islandNode = path.getNodes().get(this.distanceToIslands);
				boolean shouldBeATreasure = evaluator.isTreasurePath(path);
				assertTrue("Path " + path.getArcs() + " with score " + score + " and id " + islandNode.idOfNodeOnLayer + " on layer " + islandNode.depth + " is false positive or false negative", shouldBeATreasure == (score < 1));
				if (shouldBeATreasure) {
					numberOfTreasureSolutions++;
				}
			} catch (NoSuchElementException e) {
			}
		}
		int expectedNumberOfTreasureSolutions = (int) Math.pow(this.branchingFactor, this.depth - this.distanceToIslands) * this.numberOfIslandsWithTreasure;
		assertEquals(expectedNumberOfTreasureSolutions, numberOfTreasureSolutions);
	}
}

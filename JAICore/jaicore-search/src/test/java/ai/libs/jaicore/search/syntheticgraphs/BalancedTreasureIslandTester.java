package ai.libs.jaicore.search.syntheticgraphs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.junit.jupiter.params.provider.Arguments;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.balanced.BalancedGraphGeneratorGenerator;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.balanced.BalancedGraphSearchWithPathEvaluationsProblem;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.equalsized.EqualSizedIslandsModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.noisymean.ChaoticMeansTreasureModel;

public class BalancedTreasureIslandTester extends SyntheticGraphTester {

	//	private int numberOfIslandsWithTreasure;
	//	private int numberOfIslands;
	//	private int exactIslandSize;
	//	private IIslandModel islandModel;
	//	private ChaoticMeansTreasureModel treasureGenerator;
	//	private BalancedGraphSearchWithPathEvaluationsProblem searchProblem;

	public static Stream<Arguments> getTreeSetups() {
		final int MAX_BF = 4;
		final int MAX_DEPTH = 4;
		List<Arguments> data = new ArrayList<>();
		for (int bf = 1; bf <= MAX_BF; bf++) {
			for (int depth = 1; depth <= MAX_DEPTH; depth++) {
				int x = Math.min(3, depth);
				data.add(Arguments.of(bf, depth, x, Math.min(2, (int) Math.pow(bf, x - 1))));
			}
		}
		return data.stream();
	}

	@Override
	public IPathSearchInput<ITransparentTreeNode, Integer> getSearchProblem(final int branchingFactor, final int depth, final int distanceToIslands, final int numberOfIslandsWithTreasure) {
		ChaoticMeansTreasureModel treasureGenerator = new ChaoticMeansTreasureModel(numberOfIslandsWithTreasure, this.getIslandModel(branchingFactor, depth, distanceToIslands, numberOfIslandsWithTreasure), 0);
		treasureGenerator.setLoggerName(this.getLoggerName() + ".treasuregen");

		/* check that treasures have been distributed and that their number is correct */
		//		assertTrue(treasureGenerator.isTreasuresDistributed(), "Treasures have not been distribued.");
		//		assertEquals(numberOfIslandsWithTreasure, treasureGenerator.getTreasureIslands().size(), "The treasure model is not correct.");

		return new BalancedGraphSearchWithPathEvaluationsProblem(branchingFactor, depth, treasureGenerator);
	}

	@Override
	public IIslandModel getIslandModel(final int branchingFactor, final int depth, final int distanceToIslands, final int numberOfIslandsWithTreasure) {
		return new EqualSizedIslandsModel(BigInteger.valueOf((long) Math.pow(branchingFactor, depth - distanceToIslands)));
	}

	@Override
	public int getExpectedNumberOfIslands(final int branchingFactor, final int depth, final int distanceToIslands, final int numberOfIslandsWithTreasure) {
		return (int) Math.pow(branchingFactor, distanceToIslands);
	}

	@Override
	public int getMaximumIslandSizes(final int branchingFactor, final int depth, final int distanceToIslands, final int numberOfIslandsWithTreasure) {
		return this.getMinimumIslandSizes(branchingFactor, depth, distanceToIslands, numberOfIslandsWithTreasure);
	}

	@Override
	public int getMinimumIslandSizes(final int branchingFactor, final int depth, final int distanceToIslands, final int numberOfIslandsWithTreasure) {
		int exactIslandSize = BalancedGraphGeneratorGenerator.getNumberOfLeafsUnderANonTerminalNodeInDepth(distanceToIslands, branchingFactor, depth);
		assertEquals((int) Math.pow(branchingFactor, depth - distanceToIslands), exactIslandSize, "Island size is not computed correctly.");
		return exactIslandSize;
	}

	@Override
	public int getNumberOfTreasureIslands(final int branchingFactor, final int depth, final int distanceToIslands, final int numberOfIslandsWithTreasure) {
		return numberOfIslandsWithTreasure;
	}

	@Override
	public boolean isPathATreasure(final int branchingFactor, final int depth, final int distanceToIslands, final int numberOfIslandsWithTreasure, final ILabeledPath<ITransparentTreeNode, Integer> path) throws PathEvaluationException, InterruptedException {
		ChaoticMeansTreasureModel treasureGenerator = new ChaoticMeansTreasureModel(numberOfIslandsWithTreasure, this.getIslandModel(branchingFactor, depth, distanceToIslands, numberOfIslandsWithTreasure), 0);
		return treasureGenerator.isPathToTreasureIsland(path);
	}
}

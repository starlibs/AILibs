package ai.libs.jaicore.search.syntheticgraphs.graphmodels.balanced;

import java.math.BigInteger;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.jaicore.search.syntheticgraphs.ISyntheticTreasureIslandProblem;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.equalsized.EqualSizedIslandsModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.noisymean.ChaoticMeansTreasureModel;

public class BalancedGraphSearchWithPathEvaluationsProblem extends GraphSearchWithPathEvaluationsInput<ITransparentTreeNode, Integer, Double> implements ISyntheticTreasureIslandProblem {

	private final int expectedNumberOfIslands;
	private final int numTreasures;
	private final IIslandModel islandModel;
	private final int exactIslandSize;
	private final ChaoticMeansTreasureModel treasureModel;

	public static IIslandModel getIslandModel(final int branchingFactor, final int depth, final int distanceToIslands) {
		return new EqualSizedIslandsModel(BigInteger.valueOf((long) Math.pow(branchingFactor, depth - (double)distanceToIslands)));
	}

	public static ChaoticMeansTreasureModel getTreasureModel(final int branchingFactor, final int depth, final int distanceToIslands, final int numberOfIslandsWithTreasure) {
		return new ChaoticMeansTreasureModel(numberOfIslandsWithTreasure, getIslandModel(branchingFactor, depth, distanceToIslands), 0);
	}

	public BalancedGraphSearchWithPathEvaluationsProblem(final int branchingFactor, final int depth, final int distanceToIslands, final int numberOfIslandsWithTreasure) {
		super(new BalanceGraphSearchProblem(branchingFactor, depth), getTreasureModel(branchingFactor, depth, distanceToIslands, numberOfIslandsWithTreasure));
		this.numTreasures = numberOfIslandsWithTreasure;
		this.treasureModel = (ChaoticMeansTreasureModel)this.getPathEvaluator();
		this.treasureModel.setGraphSearchInput(this);
		this.islandModel = this.treasureModel.getIslandModel();
		this.expectedNumberOfIslands = (int) Math.pow(branchingFactor, distanceToIslands);
		this.exactIslandSize = BalancedGraphGeneratorGenerator.getNumberOfLeafsUnderANonTerminalNodeInDepth(distanceToIslands, branchingFactor, depth);
		if ((int) Math.pow(branchingFactor, depth - (double)distanceToIslands)!= this.exactIslandSize) {
			throw new IllegalStateException("Island size is not computed correctly.");
		}
	}

	@Override
	public IIslandModel getIslandModel() {
		return this.islandModel;
	}

	@Override
	public int getExpectedNumberOfIslands() {
		return this.expectedNumberOfIslands;
	}

	@Override
	public int getMaximumIslandSizes() {
		return this.exactIslandSize;
	}

	@Override
	public int getMinimumIslandSizes() {
		return this.exactIslandSize;
	}

	@Override
	public int getNumberOfTreasureIslands() {
		return this.numTreasures;
	}

	@Override
	public boolean isPathATreasure(final ILabeledPath<ITransparentTreeNode, Integer> path) throws PathEvaluationException, InterruptedException {
		return this.treasureModel.isPathToTreasureIsland(path);
	}
}

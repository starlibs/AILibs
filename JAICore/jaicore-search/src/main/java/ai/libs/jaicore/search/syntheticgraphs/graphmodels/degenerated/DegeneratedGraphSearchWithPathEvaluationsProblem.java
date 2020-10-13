package ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated;

import java.math.BigInteger;
import java.util.Random;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.jaicore.search.syntheticgraphs.ISyntheticTreasureIslandProblem;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.degenerated.DegeneratedGraphGeneratorGenerator.DegeneratedGraphGenerator;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.ITreasureModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.noisymean.ChaoticMeansTreasureModel;

public class DegeneratedGraphSearchWithPathEvaluationsProblem extends GraphSearchWithPathEvaluationsInput<ITransparentTreeNode, Integer, Double> implements ISyntheticTreasureIslandProblem {

	private final int expectedNumberOfIslands;
	private final int numTreasures;
	private final IIslandModel islandModel;
	private final int exactIslandSize;
	private final ITreasureModel treasureModel;

	public DegeneratedGraphSearchWithPathEvaluationsProblem(final Random random, final int deadEndsPerGeneration, final int branchingFactor, final int depth, final int maxIslandSize, final int numberOfIslandsWithTreasure, final IIslandModel islandModel, final ITreasureModel treasureModel) {
		super(new DegeneratedGraphSearchProblem(random, deadEndsPerGeneration, branchingFactor, depth), treasureModel);
		this.treasureModel = treasureModel;
		if (treasureModel instanceof ChaoticMeansTreasureModel) {
			((ChaoticMeansTreasureModel)treasureModel).setGraphSearchInput(this);
		}
		this.exactIslandSize = this.getGraphGenerator().getMaxNumberOfLeafsInEverySubtreeOfMaxLength(BigInteger.valueOf(maxIslandSize)).intValueExact();
		this.islandModel = islandModel;
		this.numTreasures = numberOfIslandsWithTreasure;
		this.expectedNumberOfIslands = this.getGraphGenerator().getRootGenerator().getRoots().iterator().next().getNumberOfSubtreesWithMaxNumberOfNodes(BigInteger.valueOf(maxIslandSize)).intValueExact();
		//		this.logger.info("Now considering graph with bf {}, depth {}, (exact) island size {} (max configured size was {}), and {} islands with treasure.", this.branchingFactor, this.depth, this.exactIslandSize,
		//				this.maxIslandSize.intValueExact(), this.numberOfIslandsWithTreasure.intValue());
	}

	@Override
	public DegeneratedGraphGenerator getGraphGenerator() {
		return (DegeneratedGraphGenerator)super.getGraphGenerator();
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
		return 1;
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

package ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.noisymean;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;

public class LinkedTreasureIslandPathCostGenerator extends NoisyMeanTreasureModel {
	private final Function<BigInteger, Double> meanFunction;
	private final Map<BigInteger, Double> explicitlyEvaluatedMeans = new HashMap<>();

	public LinkedTreasureIslandPathCostGenerator(final IIslandModel islandModel, final Function<BigInteger, Double> meanFunction) {
		super(islandModel);
		this.meanFunction = meanFunction;
	}

	@Override
	public double getMeanOfIsland(final BigInteger island) {
		return this.explicitlyEvaluatedMeans.computeIfAbsent(island, p -> this.meanFunction.apply(island));
	}

	@Override
	public double getMinimumAchievable() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPathToTreasureIsland(final ILabeledPath<ITransparentTreeNode, Integer> path) {
		throw new UnsupportedOperationException();
	}

}

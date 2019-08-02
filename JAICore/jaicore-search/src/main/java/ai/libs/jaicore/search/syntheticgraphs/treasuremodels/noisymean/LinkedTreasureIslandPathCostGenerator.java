package ai.libs.jaicore.search.syntheticgraphs.treasuremodels.noisymean;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;

public class LinkedTreasureIslandPathCostGenerator extends NoisyMeanTreasureModel {
	private final Function<Long, Double> meanFunction;
	private final Map<Long, Double> explicitlyEvaluatedMeans = new HashMap<>();

	public LinkedTreasureIslandPathCostGenerator(final IIslandModel islandModel, final Function<Long, Double> meanFunction) {
		super(islandModel);
		this.meanFunction = meanFunction;
	}

	@Override
	public double getMeanOfIsland(final long island) {
		return this.explicitlyEvaluatedMeans.computeIfAbsent(island, p -> this.meanFunction.apply(island));
	}

}

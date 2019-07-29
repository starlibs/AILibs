package ai.libs.jaicore.search.syntheticgraphs;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class LinkedTreasureIslandPathCostGenerator extends TreasureIslandPathCostGenerator {
	private final Function<Long, Double> meanFunction;
	private final Map<Long, Double> explicitlyEvaluatedMeans = new HashMap<>();

	public LinkedTreasureIslandPathCostGenerator(final int numberOfIslandsWithTreasure, final int distanceToIslands, final int numberOfIslands, final Function<Long, Double> meanFunction) {
		super(numberOfIslandsWithTreasure, distanceToIslands, numberOfIslands);
		this.meanFunction = meanFunction;
	}

	@Override
	public double getMeanOfIsland(final long island) {
		return this.explicitlyEvaluatedMeans.computeIfAbsent(island, p -> this.meanFunction.apply(island));
	}

}

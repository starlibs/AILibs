package ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands.noisymean;

import java.math.BigInteger;
import java.util.function.Function;

import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;

public abstract class ATreasureMeanFunction implements Function<BigInteger, Double> {

	private final IIslandModel islandModel;
	private final long numberOfTreasures;

	public ATreasureMeanFunction(final IIslandModel islandModel, final long numberOfTreasures) {
		super();
		this.islandModel = islandModel;
		this.numberOfTreasures = numberOfTreasures;
	}

	public BigInteger getTotalNumberOfIslands() {
		return this.islandModel.getNumberOfIslands();
	}

	public long getNumberOfTreasures() {
		return this.numberOfTreasures;
	}
}

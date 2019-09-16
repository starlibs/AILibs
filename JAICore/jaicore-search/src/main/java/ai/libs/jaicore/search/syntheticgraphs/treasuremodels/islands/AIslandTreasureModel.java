package ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands;

import java.math.BigInteger;

import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.ITreasureModel;

public abstract class AIslandTreasureModel implements ITreasureModel {

	private final IIslandModel islandModel;

	public AIslandTreasureModel(final IIslandModel islandModel) {
		super();
		this.islandModel = islandModel;
	}

	public IIslandModel getIslandModel() {
		return this.islandModel;
	}

	public BigInteger getTotalNumberOfIslands() {
		return this.islandModel.getNumberOfIslands();
	}
}

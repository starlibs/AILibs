package ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;
import ai.libs.jaicore.search.syntheticgraphs.treasuremodels.ITreasureModel;

public abstract class AIslandTreasureModel implements ITreasureModel {

	private final IIslandModel islandModel;
	protected Logger logger = LoggerFactory.getLogger("treasuremodel." + this.getClass().getName());

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

	@Override
	public void setLoggerName(final String loggerName) {
		this.logger = LoggerFactory.getLogger(loggerName);
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}
}

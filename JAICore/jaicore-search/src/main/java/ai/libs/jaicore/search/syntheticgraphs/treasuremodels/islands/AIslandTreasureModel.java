package ai.libs.jaicore.search.syntheticgraphs.treasuremodels.islands;

import java.math.BigInteger;

import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
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

	public BigInteger getPositionOnIsland(final ILabeledPath<ITransparentTreeNode, Integer> path) {
		return this.getIslandModel().getPositionOnIsland(path).add(BigInteger.ONE);
	}

	public BigInteger getIsland(final ILabeledPath<ITransparentTreeNode, Integer> path) {
		return this.getIslandModel().getIsland(path);
	}

	public BigInteger getIslandSize(final ILabeledPath<ITransparentTreeNode, Integer> path) {
		return this.getIslandModel().getSizeOfIsland(path);
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

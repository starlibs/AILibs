package ai.libs.jaicore.search.syntheticgraphs.islandmodels.equalsized;

import org.api4.java.datastructure.graph.IPath;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;

public class EqualSizedIslandsModel implements IIslandModel {

	private final long size;
	private long numberOfIslands = -1;
	private ITransparentTreeNode rootNode;

	public EqualSizedIslandsModel(final long size) {
		super();
		this.size = size;
	}

	@Override
	public long getIsland(final IPath<ITransparentTreeNode, Integer> path) {
		long numberOfLeafsOnTheLeft = path.getHead().getNumberOfLeafsStemmingFromLeftRelativesInSameGeneration();
		if (numberOfLeafsOnTheLeft == 0) {
			return 0;
		}
		return (long)Math.floor(numberOfLeafsOnTheLeft * 1.0 / this.size);
	}

	@Override
	public long getNumberOfIslands() {
		if (this.numberOfIslands < 0) {
			this.numberOfIslands = (long)Math.ceil(this.rootNode.getNumberOfLeafsUnderNode() * 1.0 / this.size);
		}
		return this.numberOfIslands;
	}

	@Override
	public void setRootNode(final ITransparentTreeNode root) {
		this.rootNode = root;
	}
}

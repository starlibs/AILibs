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
		long island = path.getHead().getNumberOfSubtreesWithMaxNumberOfNodesPriorToThisNode(this.size);
		//		System.out.println(path + ": " + island);
		return island;
	}

	@Override
	public long getNumberOfIslands() {
		return this.rootNode.getNumberOfSubtreesWithMaxNumberOfNodes(this.size);
	}

	@Override
	public void setRootNode(final ITransparentTreeNode root) {
		this.rootNode = root;
	}
}

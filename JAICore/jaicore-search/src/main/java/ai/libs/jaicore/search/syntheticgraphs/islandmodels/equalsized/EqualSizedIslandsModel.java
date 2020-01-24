package ai.libs.jaicore.search.syntheticgraphs.islandmodels.equalsized;

import java.math.BigInteger;

import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;
import ai.libs.jaicore.search.syntheticgraphs.islandmodels.IIslandModel;

public class EqualSizedIslandsModel implements IIslandModel {

	private final BigInteger size;
	private long numberOfIslands = -1;
	private ITransparentTreeNode rootNode;

	public EqualSizedIslandsModel(final int size) {
		this(BigInteger.valueOf(size));
	}

	public EqualSizedIslandsModel(final BigInteger size) {
		super();
		this.size = size;
	}

	@Override
	public BigInteger getIsland(final ILabeledPath<ITransparentTreeNode, Integer> path) {
		return path.getHead().getNumberOfSubtreesWithMaxNumberOfNodesPriorToThisNode(this.size);
	}

	@Override
	public BigInteger getNumberOfIslands() {
		if (this.rootNode == null) {
			throw new IllegalStateException("Root has not been initialized yet!");
		}
		return this.rootNode.getNumberOfSubtreesWithMaxNumberOfNodes(this.size);
	}

	@Override
	public void setRootNode(final ITransparentTreeNode root) {
		this.rootNode = root;
	}

	@Override
	public BigInteger getSizeOfIsland(final ILabeledPath<ITransparentTreeNode, Integer> path) {
		ILabeledPath<ITransparentTreeNode, Integer> currentPath = path;
		while (!currentPath.getArcs().isEmpty() && currentPath.getPathToParentOfHead().getHead().getNumberOfLeafsUnderNode().compareTo(this.size) <= 0) {
			currentPath = currentPath.getPathToParentOfHead();
		}
		BigInteger sizeOfThisIsland = currentPath.getHead().getNumberOfLeafsUnderNode();
		return sizeOfThisIsland;
	}

	@Override
	public BigInteger getPositionOnIsland(final ILabeledPath<ITransparentTreeNode, Integer> path) {
		return path.getHead().getNumberOfLeafsPriorToNodeViaDFS().subtract(path.getHead().getNumberOfLeafsInSubtreesWithMaxNumberOfNodesPriorToThisNode(this.size));
	}
}

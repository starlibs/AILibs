package ai.libs.jaicore.search.syntheticgraphs.graphmodels;

import java.math.BigInteger;

public interface ITransparentTreeNode {

	public int getDepth();

	public BigInteger getNumberOfLeftRelativesInSameGeneration();

	public BigInteger getNumberOfRightRelativesInSameGeneration();

	public BigInteger getNumberOfSubtreesWithMaxNumberOfNodesPriorToThisNode(BigInteger maxNumberOfNodes);

	/**
	 *  Gets the number of leaf nodes of all sub-trees of maximum given size prior to the node on which it is invoked.
	 *
	 *  Note that the leafs in the same sub-tree are not counted.
	 *  These can be obtained by computing the number of all leaf nodes prior to this one minus the result of this method.
	 *
	 * @param maxNumberOfNodes
	 * @return
	 */
	public BigInteger getNumberOfLeafsInSubtreesWithMaxNumberOfNodesPriorToThisNode(BigInteger maxNumberOfNodes);

	public BigInteger getNumberOfSubtreesWithMaxNumberOfNodes(BigInteger maxNumberOfNodes);

	public BigInteger getNumberOfLeafsPriorToNodeViaDFS();

	public BigInteger getNumberOfLeafsStemmingFromLeftRelativesInSameGeneration(); // siblings that are leafs should count as 1

	public BigInteger getNumberOfLeafsUnderNode();

	public BigInteger getNumberOfLeafsStemmingFromRightRelativesInSameGeneration(); // siblings that are leafs should count as 1

	public int getDistanceToShallowestLeafUnderNode();

	public int getDistanceToDeepestLeafUnderNode();

	public boolean hasChildren();
}

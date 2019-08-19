package ai.libs.jaicore.search.syntheticgraphs.graphmodels;

public interface ITransparentTreeNode {

	public int getDepth();

	public long getNumberOfLeftRelativesInSameGeneration();

	public long getNumberOfRightRelativesInSameGeneration();

	public long getNumberOfSubtreesWithMaxNumberOfNodesPriorToThisNode(long maxNumberOfNodes);
	public long getNumberOfSubtreesWithMaxNumberOfNodes(long maxNumberOfNodes);

	public long getNumberOfLeafsPriorToNodeViaDFS();

	public long getNumberOfLeafsStemmingFromLeftRelativesInSameGeneration(); // siblings that are leafs should count as 1

	public long getNumberOfLeafsUnderNode();

	public long getNumberOfLeafsStemmingFromRightRelativesInSameGeneration(); // siblings that are leafs should count as 1

	public int getDistanceToShallowestLeafUnderNode();

	public int getDistanceToDeepestLeafUnderNode();
}

package ai.libs.jaicore.search.syntheticgraphs.islandmodels;

import java.math.BigInteger;

import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;


/**
 * Gives information about the number of islands and the id of the island to which a particular path leads.
 *
 * setRootNode or getIsland must be invoked before getNumberOfIslands is called
 *
 * @author fmohr
 *
 */
public interface IIslandModel {

	public void setRootNode(ITransparentTreeNode root);

	public BigInteger getIsland(ILabeledPath<ITransparentTreeNode, Integer> path);

	public BigInteger getSizeOfIsland(ILabeledPath<ITransparentTreeNode, Integer> path);

	public BigInteger getPositionOnIsland(ILabeledPath<ITransparentTreeNode, Integer> path);

	public BigInteger getNumberOfIslands();
}

package ai.libs.jaicore.search.syntheticgraphs.islandmodels;

import org.api4.java.datastructure.graph.IPath;

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

	public long getIsland(IPath<ITransparentTreeNode, Integer> path);

	public long getNumberOfIslands();
}

package jaicore.graphvisualizer.plugin.nodeinfo;

/**
 * 
 * @author hetzer
 *
 * @param <N>
 *            The node class for which information is provided
 */
public interface NodeInfoGenerator<N> {

	public String generateInfoForNode(N node);
}

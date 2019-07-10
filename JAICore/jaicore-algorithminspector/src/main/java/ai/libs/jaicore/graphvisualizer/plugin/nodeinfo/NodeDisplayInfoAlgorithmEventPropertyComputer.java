package ai.libs.jaicore.graphvisualizer.plugin.nodeinfo;

import org.api4.java.algorithm.events.AlgorithmEvent;

import ai.libs.jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.PropertyComputationFailedException;

public class NodeDisplayInfoAlgorithmEventPropertyComputer<N> implements AlgorithmEventPropertyComputer {

	public static final String NODE_DISPLAY_INFO_PROPERTY_NAME = "node_display_info";

	private NodeInfoGenerator<N> nodeInfoGenerator;

	public NodeDisplayInfoAlgorithmEventPropertyComputer(final NodeInfoGenerator<N> nodeInfoGenerator) {
		this.nodeInfoGenerator = nodeInfoGenerator;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String computeAlgorithmEventProperty(final AlgorithmEvent algorithmEvent) throws PropertyComputationFailedException {
		String nodeInfo = null;
		if (algorithmEvent instanceof GraphInitializedEvent) {
			GraphInitializedEvent<N> graphInitializedEvent = (GraphInitializedEvent<N>) algorithmEvent;
			return this.getNodeInfoForNodeIfTypeFits(graphInitializedEvent.getRoot());
		} else if (algorithmEvent instanceof NodeAddedEvent) {
			NodeAddedEvent<N> nodeAddedEvent = (NodeAddedEvent<N>) algorithmEvent;
			return this.getNodeInfoForNodeIfTypeFits(nodeAddedEvent.getNode());
		}
		return nodeInfo;
	}

	@Override
	public String getPropertyName() {
		return NODE_DISPLAY_INFO_PROPERTY_NAME;
	}

	private String getNodeInfoForNodeIfTypeFits(final N node) {
		return this.nodeInfoGenerator.generateInfoForNode(node);

	}

}
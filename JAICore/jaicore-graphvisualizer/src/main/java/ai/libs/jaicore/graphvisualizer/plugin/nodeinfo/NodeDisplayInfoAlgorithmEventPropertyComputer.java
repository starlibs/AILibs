package ai.libs.jaicore.graphvisualizer.plugin.nodeinfo;

import ai.libs.jaicore.basic.algorithm.events.AlgorithmEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.PropertyComputationFailedException;

public class NodeDisplayInfoAlgorithmEventPropertyComputer<N> implements AlgorithmEventPropertyComputer {

	public static final String NODE_DISPLAY_INFO_PROPERTY_NAME = "node_display_info";

	private NodeInfoGenerator<N> nodeInfoGenerator;

	public NodeDisplayInfoAlgorithmEventPropertyComputer(NodeInfoGenerator<N> nodeInfoGenerator) {
		this.nodeInfoGenerator = nodeInfoGenerator;
	}

	@Override
	public String computeAlgorithmEventProperty(AlgorithmEvent algorithmEvent) throws PropertyComputationFailedException {
		String nodeInfo = null;
		if (algorithmEvent instanceof GraphInitializedEvent) {
			GraphInitializedEvent<N> graphInitializedEvent = (GraphInitializedEvent<N>) algorithmEvent;
			return getNodeInfoForNodeIfTypeFits(graphInitializedEvent.getRoot());
		} else if (algorithmEvent instanceof NodeAddedEvent) {
			NodeAddedEvent<N> nodeAddedEvent = (NodeAddedEvent<N>) algorithmEvent;
			return getNodeInfoForNodeIfTypeFits(nodeAddedEvent.getNode());
		}
		return nodeInfo;
	}

	@Override
	public String getPropertyName() {
		return NODE_DISPLAY_INFO_PROPERTY_NAME;
	}

	private String getNodeInfoForNodeIfTypeFits(N node) {
		return nodeInfoGenerator.generateInfoForNode(node);

	}

}

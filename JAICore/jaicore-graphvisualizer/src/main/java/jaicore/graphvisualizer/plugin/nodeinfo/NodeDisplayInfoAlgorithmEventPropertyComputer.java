package jaicore.graphvisualizer.plugin.nodeinfo;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import jaicore.graphvisualizer.events.recorder.property.PropertyComputationFailedException;

public class NodeDisplayInfoAlgorithmEventPropertyComputer<N> implements AlgorithmEventPropertyComputer {

	public static final String NODE_INFO_PROPERTY_NAME = "node_display_info";

	private NodeInfoGenerator<N> nodeInfoGenerator;

	private Class<N> nodeClass;

	public NodeDisplayInfoAlgorithmEventPropertyComputer(NodeInfoGenerator<N> nodeInfoGenerator, Class<N> nodeClass) {
		this.nodeInfoGenerator = nodeInfoGenerator;
		this.nodeClass = nodeClass;
	}

	@Override
	public String computeAlgorithmEventProperty(AlgorithmEvent algorithmEvent) throws PropertyComputationFailedException {
		String nodeInfo = null;
		if (algorithmEvent instanceof GraphInitializedEvent) {
			GraphInitializedEvent<?> graphInitializedEvent = (GraphInitializedEvent<?>) algorithmEvent;
			return getNodeInfoForNodeIfTypeFits(graphInitializedEvent.getRoot());
		} else if (algorithmEvent instanceof NodeAddedEvent) {
			NodeAddedEvent<?> nodeAddedEvent = (NodeAddedEvent<?>) algorithmEvent;
			return getNodeInfoForNodeIfTypeFits(nodeAddedEvent.getNode());
		}
		return nodeInfo;
	}

	@Override
	public String getPropertyName() {
		return NODE_INFO_PROPERTY_NAME;
	}

	private String getNodeInfoForNodeIfTypeFits(Object rawNode) {
		if (nodeClass.isAssignableFrom(rawNode.getClass())) {
			@SuppressWarnings("unchecked")
			N node = (N) rawNode;
			return nodeInfoGenerator.generateInfoForNode(node);
		}
		return null;
	}

}

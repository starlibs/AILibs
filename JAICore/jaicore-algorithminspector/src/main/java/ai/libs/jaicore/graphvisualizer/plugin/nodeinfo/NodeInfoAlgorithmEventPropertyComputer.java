package ai.libs.jaicore.graphvisualizer.plugin.nodeinfo;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import ai.libs.jaicore.basic.algorithm.events.AlgorithmEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeRemovedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeTypeSwitchEvent;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.PropertyComputationFailedException;

public class NodeInfoAlgorithmEventPropertyComputer implements AlgorithmEventPropertyComputer {

	public static final String NODE_INFO_PROPERTY_NAME = "node_info";

	private Map<Object, Integer> nodeToIdMap;
	private AtomicInteger idCounter;

	public NodeInfoAlgorithmEventPropertyComputer() {
		nodeToIdMap = new ConcurrentHashMap<>();
		idCounter = new AtomicInteger();
	}

	@Override
	public NodeInfo computeAlgorithmEventProperty(AlgorithmEvent algorithmEvent) throws PropertyComputationFailedException {
		if (algorithmEvent instanceof GraphInitializedEvent) {
			GraphInitializedEvent<?> graphInitializedEvent = (GraphInitializedEvent<?>) algorithmEvent;
			Object mainNode = graphInitializedEvent.getRoot();
			return new NodeInfo(getIdOfNode(mainNode), null, null, null);
		} else if (algorithmEvent instanceof NodeAddedEvent) {
			NodeAddedEvent<?> nodeAddedEvent = (NodeAddedEvent<?>) algorithmEvent;
			Object mainNode = nodeAddedEvent.getNode();
			String mainNodeId = getIdOfNode(mainNode);
			String parentNodeId = getIdOfNode(nodeAddedEvent.getParent());
			String nodeType = nodeAddedEvent.getType();
			return new NodeInfo(mainNodeId, Arrays.asList(parentNodeId), null, nodeType);
		} else if (algorithmEvent instanceof NodeRemovedEvent) {
			NodeRemovedEvent<?> nodeRemovedEvent = (NodeRemovedEvent<?>) algorithmEvent;
			String mainNodeId = getIdOfNode(nodeRemovedEvent.getNode());
			return new NodeInfo(mainNodeId, null, null, null);
		} else if (algorithmEvent instanceof NodeTypeSwitchEvent) {
			NodeTypeSwitchEvent<?> nodeAddedEvent = (NodeTypeSwitchEvent<?>) algorithmEvent;
			Object mainNode = nodeAddedEvent.getNode();
			String mainNodeId = getIdOfNode(mainNode);
			String nodeType = nodeAddedEvent.getType();
			return new NodeInfo(mainNodeId, null, null, nodeType);
		}
		return null;
	}

	@Override
	public String getPropertyName() {
		return NODE_INFO_PROPERTY_NAME;
	}

	private String getIdOfNode(Object node) {
		if (!nodeToIdMap.containsKey(node)) {
			int nodeId = idCounter.getAndIncrement();
			nodeToIdMap.put(node, nodeId);
		}
		return String.valueOf(nodeToIdMap.get(node));
	}

	public String getIdOfNodeIfExistent(Object node) {
		return String.valueOf(nodeToIdMap.get(node));
	}

}

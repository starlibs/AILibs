package ai.libs.jaicore.graphvisualizer.plugin.nodeinfo;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.api4.java.algorithm.events.IAlgorithmEvent;

import ai.libs.jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeInfoAlteredEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeRemovedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeTypeSwitchEvent;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.PropertyComputationFailedException;

public class NodeInfoAlgorithmEventPropertyComputer implements AlgorithmEventPropertyComputer {

	public static final String NODE_INFO_PROPERTY_NAME = "node_info";

	private Map<Object, Integer> nodeToIdMap;
	private AtomicInteger idCounter;

	public NodeInfoAlgorithmEventPropertyComputer() {
		this.nodeToIdMap = new ConcurrentHashMap<>();
		this.idCounter = new AtomicInteger();
	}

	@Override
	public NodeInfo computeAlgorithmEventProperty(final IAlgorithmEvent algorithmEvent) throws PropertyComputationFailedException {
		if (algorithmEvent instanceof GraphInitializedEvent) {
			GraphInitializedEvent<?> graphInitializedEvent = (GraphInitializedEvent<?>) algorithmEvent;
			Object mainNode = graphInitializedEvent.getRoot();
			return new NodeInfo(this.getIdOfNode(mainNode), null, null, null);
		} else if (algorithmEvent instanceof NodeAddedEvent) {
			NodeAddedEvent<?> nodeAddedEvent = (NodeAddedEvent<?>) algorithmEvent;
			Object mainNode = nodeAddedEvent.getNode();
			String mainNodeId = this.getIdOfNode(mainNode);
			String parentNodeId = this.getIdOfNode(nodeAddedEvent.getParent());
			String nodeType = nodeAddedEvent.getType();
			return new NodeInfo(mainNodeId, Arrays.asList(parentNodeId), null, nodeType);
		} else if (algorithmEvent instanceof NodeInfoAlteredEvent) {
			NodeInfoAlteredEvent<?> nodeAddedEvent = (NodeInfoAlteredEvent<?>) algorithmEvent;
			Object mainNode = nodeAddedEvent.getNode();
			String mainNodeId = this.getIdOfNode(mainNode);
			return new NodeInfo(mainNodeId, null, null, null);
		} else if (algorithmEvent instanceof NodeRemovedEvent) {
			NodeRemovedEvent<?> nodeRemovedEvent = (NodeRemovedEvent<?>) algorithmEvent;
			String mainNodeId = this.getIdOfNode(nodeRemovedEvent.getNode());
			return new NodeInfo(mainNodeId, null, null, null);
		} else if (algorithmEvent instanceof NodeTypeSwitchEvent) {
			NodeTypeSwitchEvent<?> nodeAddedEvent = (NodeTypeSwitchEvent<?>) algorithmEvent;
			Object mainNode = nodeAddedEvent.getNode();
			String mainNodeId = this.getIdOfNode(mainNode);
			String nodeType = nodeAddedEvent.getType();
			return new NodeInfo(mainNodeId, null, null, nodeType);
		}
		return null;
	}

	@Override
	public String getPropertyName() {
		return NODE_INFO_PROPERTY_NAME;
	}

	private String getIdOfNode(final Object node) {
		if (!this.nodeToIdMap.containsKey(node)) {
			int nodeId = this.idCounter.getAndIncrement();
			this.nodeToIdMap.put(node, nodeId);
		}
		return String.valueOf(this.nodeToIdMap.get(node));
	}

	public String getIdOfNodeIfExistent(final Object node) {
		return String.valueOf(this.nodeToIdMap.get(node));
	}

}

package ai.libs.jaicore.graphvisualizer.plugin.nodeinfo;

import java.util.Arrays;
import java.util.List;

import org.api4.java.algorithm.events.IAlgorithmEvent;

import ai.libs.jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeInfoAlteredEvent;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.PropertyComputationFailedException;

public class NodeDisplayInfoAlgorithmEventPropertyComputer<N> implements AlgorithmEventPropertyComputer {

	public static final String NODE_DISPLAY_INFO_PROPERTY_NAME = "node_display_info";

	private NodeInfoGenerator<N> nodeInfoGenerator;
	private String propertyName;

	public NodeDisplayInfoAlgorithmEventPropertyComputer(final NodeInfoGenerator<N> nodeInfoGenerator) {
		this.nodeInfoGenerator = nodeInfoGenerator;
		this.propertyName = NODE_DISPLAY_INFO_PROPERTY_NAME;
	}

	public NodeDisplayInfoAlgorithmEventPropertyComputer(final String propertyName, final NodeInfoGenerator<N> nodeInfoGenerator) {
		this.propertyName = propertyName;
		this.nodeInfoGenerator = nodeInfoGenerator;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String computeAlgorithmEventProperty(final IAlgorithmEvent algorithmEvent) throws PropertyComputationFailedException {
		String nodeInfo = null;
		if (algorithmEvent instanceof GraphInitializedEvent) {
			GraphInitializedEvent<N> graphInitializedEvent = (GraphInitializedEvent<N>) algorithmEvent;
			return this.getNodeInfoForNodeIfTypeFits(graphInitializedEvent.getRoot());
		} else if (algorithmEvent instanceof NodeAddedEvent) {
			NodeAddedEvent<N> nodeAddedEvent = (NodeAddedEvent<N>) algorithmEvent;
			return this.getNodeInfoForNodeIfTypeFits(nodeAddedEvent.getNode());
		} else if (algorithmEvent instanceof NodeInfoAlteredEvent) {
			NodeInfoAlteredEvent<N> nodeAddedEvent = (NodeInfoAlteredEvent<N>) algorithmEvent;
			return this.getNodeInfoForNodeIfTypeFits(nodeAddedEvent.getNode());
		}
		return nodeInfo;
	}

	@Override
	public String getPropertyName() {
		return this.propertyName;
	}

	private String getNodeInfoForNodeIfTypeFits(final N node) {
		return this.nodeInfoGenerator.generateInfoForNode(node);

	}

	@Override
	public List<AlgorithmEventPropertyComputer> getRequiredPropertyComputers() {
		return Arrays.asList();
	}

	@Override
	public void overwriteRequiredPropertyComputer(final AlgorithmEventPropertyComputer computer) {
		throw new UnsupportedOperationException(this.getClass().getCanonicalName() + " has no dependencies to other property computers.");
	}

}

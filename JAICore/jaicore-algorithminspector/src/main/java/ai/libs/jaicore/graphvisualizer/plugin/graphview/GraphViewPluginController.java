package ai.libs.jaicore.graphvisualizer.plugin.graphview;

import java.util.Collections;
import java.util.stream.Collectors;

import org.api4.java.algorithm.events.serializable.PropertyProcessedAlgorithmEvent;

import ai.libs.jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeRemovedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeTypeSwitchEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginController;
import ai.libs.jaicore.graphvisualizer.plugin.controlbar.ResetEvent;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfo;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.timeslider.GoToTimeStepEvent;

public class GraphViewPluginController implements IGUIPluginController {

	private GraphViewPluginModel model;

	public GraphViewPluginController(final GraphViewPluginModel model) {
		this.model = model;
	}

	@Override
	public void handleSerializableAlgorithmEvent(final PropertyProcessedAlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		try {
			if (this.correspondsToGraphInitializedEvent(algorithmEvent)) {
				this.handleGraphInitializedEvent(algorithmEvent);
			} else if (this.correspondsToNodeAddedEvent(algorithmEvent)) {
				this.handleNodeAddedEvent(algorithmEvent);
			} else if (this.correspondsToNodeRemovedEvent(algorithmEvent)) {
				this.handleNodeRemovedEvent(algorithmEvent);
			} else if (this.correspondsToNodeTypSwitchEvent(algorithmEvent)) {
				this.handleNodeTypeSwitchEvent(algorithmEvent);
			}
		} catch (ViewGraphManipulationException exception) {
			throw new HandleAlgorithmEventException("Encountered a problem while handling graph event " + algorithmEvent + " .", exception);
		}
	}

	private boolean correspondsToNodeTypSwitchEvent(final PropertyProcessedAlgorithmEvent algorithmEvent) {
		return algorithmEvent.getEventName().equalsIgnoreCase(NodeTypeSwitchEvent.class.getSimpleName());
	}

	private boolean correspondsToNodeRemovedEvent(final PropertyProcessedAlgorithmEvent algorithmEvent) {
		return algorithmEvent.getEventName().equalsIgnoreCase(NodeRemovedEvent.class.getSimpleName());
	}

	private boolean correspondsToNodeAddedEvent(final PropertyProcessedAlgorithmEvent algorithmEvent) {
		return algorithmEvent.getEventName().equalsIgnoreCase(NodeAddedEvent.class.getSimpleName());
	}

	private boolean correspondsToGraphInitializedEvent(final PropertyProcessedAlgorithmEvent algorithmEvent) {
		return algorithmEvent.getEventName().equalsIgnoreCase(GraphInitializedEvent.class.getSimpleName());
	}

	private void handleGraphInitializedEvent(final PropertyProcessedAlgorithmEvent graphInitializedEvent) throws ViewGraphManipulationException {
		NodeInfo nodeInfo = graphInitializedEvent.getProperty(NodeInfoAlgorithmEventPropertyComputer.NODE_INFO_PROPERTY_NAME, NodeInfo.class);
		this.model.addNode(nodeInfo.getMainNodeId(), Collections.emptyList(), "root");
	}

	private void handleNodeAddedEvent(final PropertyProcessedAlgorithmEvent nodeReachedEvent) throws ViewGraphManipulationException {
		NodeInfo nodeInfo = nodeReachedEvent.getProperty(NodeInfoAlgorithmEventPropertyComputer.NODE_INFO_PROPERTY_NAME, NodeInfo.class);
		this.model.addNode(nodeInfo.getMainNodeId(), nodeInfo.getParentNodeIds().stream().map(s -> (Object) s).collect(Collectors.toList()), nodeInfo.getNodeType());
	}

	private void handleNodeTypeSwitchEvent(final PropertyProcessedAlgorithmEvent nodeTypeSwitchEvent) throws ViewGraphManipulationException {
		NodeInfo nodeInfo = nodeTypeSwitchEvent.getProperty(NodeInfoAlgorithmEventPropertyComputer.NODE_INFO_PROPERTY_NAME, NodeInfo.class);
		this.model.switchNodeType(nodeInfo.getMainNodeId(), nodeInfo.getNodeType());
	}

	private void handleNodeRemovedEvent(final PropertyProcessedAlgorithmEvent nodeRemovedEvent) throws ViewGraphManipulationException {
		NodeInfo nodeInfo = nodeRemovedEvent.getProperty(NodeInfoAlgorithmEventPropertyComputer.NODE_INFO_PROPERTY_NAME, NodeInfo.class);
		this.model.removeNode(nodeInfo.getMainNodeId());
	}

	@Override
	public void handleGUIEvent(final GUIEvent guiEvent) {
		if (guiEvent instanceof ResetEvent) {
			this.model.reset();
		} else if (guiEvent instanceof GoToTimeStepEvent) {
			this.model.reset();
		}
	}

}

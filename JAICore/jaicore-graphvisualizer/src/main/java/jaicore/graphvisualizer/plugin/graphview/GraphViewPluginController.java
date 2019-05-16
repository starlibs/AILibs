package jaicore.graphvisualizer.plugin.graphview;

import java.util.Collections;
import java.util.stream.Collectors;

import jaicore.basic.algorithm.events.serializable.PropertyProcessedAlgorithmEvent;
import jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import jaicore.graphvisualizer.events.graph.NodeRemovedEvent;
import jaicore.graphvisualizer.events.graph.NodeTypeSwitchEvent;
import jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import jaicore.graphvisualizer.events.gui.GUIEvent;
import jaicore.graphvisualizer.plugin.IGUIPluginController;
import jaicore.graphvisualizer.plugin.controlbar.ResetEvent;
import jaicore.graphvisualizer.plugin.nodeinfo.NodeInfo;
import jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoAlgorithmEventPropertyComputer;
import jaicore.graphvisualizer.plugin.timeslider.GoToTimeStepEvent;

public class GraphViewPluginController implements IGUIPluginController {

	private GraphViewPluginModel model;

	public GraphViewPluginController(GraphViewPluginModel model) {
		this.model = model;
	}

	@Override
	public void handleSerializableAlgorithmEvent(PropertyProcessedAlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		try {
			if (correspondsToGraphInitializedEvent(algorithmEvent)) {
				handleGraphInitializedEvent(algorithmEvent);
			} else if (correspondsToNodeAddedEvent(algorithmEvent)) {
				handleNodeAddedEvent(algorithmEvent);
			} else if (correspondsToNodeRemovedEvent(algorithmEvent)) {
				handleNodeRemovedEvent(algorithmEvent);
			} else if (correspondsToNodeTypSwitchEvent(algorithmEvent)) {
				handleNodeTypeSwitchEvent(algorithmEvent);
			}
		} catch (ViewGraphManipulationException exception) {
			throw new HandleAlgorithmEventException("Encountered a problem while handling graph event " + algorithmEvent + " .", exception);
		}
	}

	private boolean correspondsToNodeTypSwitchEvent(PropertyProcessedAlgorithmEvent algorithmEvent) {
		return algorithmEvent.getEventName().equalsIgnoreCase(NodeTypeSwitchEvent.class.getSimpleName());
	}

	private boolean correspondsToNodeRemovedEvent(PropertyProcessedAlgorithmEvent algorithmEvent) {
		return algorithmEvent.getEventName().equalsIgnoreCase(NodeRemovedEvent.class.getSimpleName());
	}

	private boolean correspondsToNodeAddedEvent(PropertyProcessedAlgorithmEvent algorithmEvent) {
		return algorithmEvent.getEventName().equalsIgnoreCase(NodeAddedEvent.class.getSimpleName());
	}

	private boolean correspondsToGraphInitializedEvent(PropertyProcessedAlgorithmEvent algorithmEvent) {
		return algorithmEvent.getEventName().equalsIgnoreCase(GraphInitializedEvent.class.getSimpleName());
	}

	private void handleGraphInitializedEvent(PropertyProcessedAlgorithmEvent graphInitializedEvent) throws ViewGraphManipulationException {
		NodeInfo nodeInfo = graphInitializedEvent.getProperty(NodeInfoAlgorithmEventPropertyComputer.NODE_INFO_PROPERTY_NAME, NodeInfo.class);
		model.addNode(nodeInfo.getMainNodeId(), Collections.emptyList(), "root");
	}

	private void handleNodeAddedEvent(PropertyProcessedAlgorithmEvent nodeReachedEvent) throws ViewGraphManipulationException {
		NodeInfo nodeInfo = nodeReachedEvent.getProperty(NodeInfoAlgorithmEventPropertyComputer.NODE_INFO_PROPERTY_NAME, NodeInfo.class);
		model.addNode(nodeInfo.getMainNodeId(), nodeInfo.getParentNodeIds().stream().map(s -> (Object) s).collect(Collectors.toList()), nodeInfo.getNodeType());
	}

	private void handleNodeTypeSwitchEvent(PropertyProcessedAlgorithmEvent nodeTypeSwitchEvent) throws ViewGraphManipulationException {
		NodeInfo nodeInfo = nodeTypeSwitchEvent.getProperty(NodeInfoAlgorithmEventPropertyComputer.NODE_INFO_PROPERTY_NAME, NodeInfo.class);
		model.switchNodeType(nodeInfo.getMainNodeId(), nodeInfo.getNodeType());
	}

	private void handleNodeRemovedEvent(PropertyProcessedAlgorithmEvent nodeRemovedEvent) throws ViewGraphManipulationException {
		NodeInfo nodeInfo = nodeRemovedEvent.getProperty(NodeInfoAlgorithmEventPropertyComputer.NODE_INFO_PROPERTY_NAME, NodeInfo.class);
		model.removeNode(nodeInfo.getMainNodeId());
	}

	@Override
	public void handleGUIEvent(GUIEvent guiEvent) {
		if (guiEvent instanceof ResetEvent) {
			model.reset();
		} else if (guiEvent instanceof GoToTimeStepEvent) {
			model.reset();
		}
	}

}

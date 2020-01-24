package ai.libs.jaicore.graphvisualizer.plugin.graphview;

import java.util.Collections;
import java.util.stream.Collectors;

import org.api4.java.algorithm.events.serializable.IPropertyProcessedAlgorithmEvent;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class GraphViewPluginController implements IGUIPluginController, ILoggingCustomizable {

	private GraphViewPluginModel model;
	private Logger logger = LoggerFactory.getLogger(GraphViewPluginController.class);

	public GraphViewPluginController(final GraphViewPluginModel model) {
		this.model = model;
	}

	@Override
	public void handleSerializableAlgorithmEvent(final IPropertyProcessedAlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
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

	private boolean correspondsToNodeTypSwitchEvent(final IPropertyProcessedAlgorithmEvent algorithmEvent) {
		return algorithmEvent.getEventName().equalsIgnoreCase(NodeTypeSwitchEvent.class.getSimpleName());
	}

	private boolean correspondsToNodeRemovedEvent(final IPropertyProcessedAlgorithmEvent algorithmEvent) {
		return algorithmEvent.getEventName().equalsIgnoreCase(NodeRemovedEvent.class.getSimpleName());
	}

	private boolean correspondsToNodeAddedEvent(final IPropertyProcessedAlgorithmEvent algorithmEvent) {
		return algorithmEvent.getEventName().equalsIgnoreCase(NodeAddedEvent.class.getSimpleName());
	}

	private boolean correspondsToGraphInitializedEvent(final IPropertyProcessedAlgorithmEvent algorithmEvent) {
		return algorithmEvent.getEventName().equalsIgnoreCase(GraphInitializedEvent.class.getSimpleName());
	}

	private void handleGraphInitializedEvent(final IPropertyProcessedAlgorithmEvent graphInitializedEvent) throws ViewGraphManipulationException {
		NodeInfo nodeInfo = graphInitializedEvent.getProperty(NodeInfoAlgorithmEventPropertyComputer.NODE_INFO_PROPERTY_NAME, NodeInfo.class);
		this.model.addNode(nodeInfo.getMainNodeId(), Collections.emptyList(), "root");
	}

	private void handleNodeAddedEvent(final IPropertyProcessedAlgorithmEvent nodeReachedEvent) throws ViewGraphManipulationException {
		NodeInfo nodeInfo = nodeReachedEvent.getProperty(NodeInfoAlgorithmEventPropertyComputer.NODE_INFO_PROPERTY_NAME, NodeInfo.class);
		this.model.addNode(nodeInfo.getMainNodeId(), nodeInfo.getParentNodeIds().stream().map(s -> (Object) s).collect(Collectors.toList()), nodeInfo.getNodeType());
	}

	private void handleNodeTypeSwitchEvent(final IPropertyProcessedAlgorithmEvent nodeTypeSwitchEvent) throws ViewGraphManipulationException {
		NodeInfo nodeInfo = nodeTypeSwitchEvent.getProperty(NodeInfoAlgorithmEventPropertyComputer.NODE_INFO_PROPERTY_NAME, NodeInfo.class);
		this.model.switchNodeType(nodeInfo.getMainNodeId(), nodeInfo.getNodeType());
	}

	private void handleNodeRemovedEvent(final IPropertyProcessedAlgorithmEvent nodeRemovedEvent) throws ViewGraphManipulationException {
		NodeInfo nodeInfo = nodeRemovedEvent.getProperty(NodeInfoAlgorithmEventPropertyComputer.NODE_INFO_PROPERTY_NAME, NodeInfo.class);
		this.model.removeNode(nodeInfo.getMainNodeId());
	}

	@Override
	public void handleGUIEvent(final GUIEvent guiEvent) {
		if (guiEvent instanceof ResetEvent || guiEvent instanceof GoToTimeStepEvent) {
			this.model.reset();
		}
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}

}

package ai.libs.jaicore.graphvisualizer.plugin.graphview;

import java.util.Collections;
import java.util.stream.Collectors;

import org.api4.java.algorithm.events.serializable.IPropertyProcessedAlgorithmEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodePropertyChangedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeRemovedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeTypeSwitchEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginController;
import ai.libs.jaicore.graphvisualizer.plugin.controlbar.ResetEvent;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfo;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.timeslider.GoToTimeStepEvent;

public class GraphViewPluginController extends ASimpleMVCPluginController<GraphViewPluginModel, GraphViewPluginView> {

	private Logger logger = LoggerFactory.getLogger(GraphViewPluginController.class);

	public GraphViewPluginController(final GraphViewPluginModel model, final GraphViewPluginView view) {
		super(model, view);
	}

	@Override
	protected void handleAlgorithmEventInternally(final IPropertyProcessedAlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		try {
			if (this.correspondsToGraphInitializedEvent(algorithmEvent)) {
				this.handleGraphInitializedEvent(algorithmEvent);
			} else if (this.correspondsToNodeAddedEvent(algorithmEvent)) {
				this.handleNodeAddedEvent(algorithmEvent);
			} else if (this.correspondsToNodeRemovedEvent(algorithmEvent)) {
				this.handleNodeRemovedEvent(algorithmEvent);
			} else if (this.correspondsToNodeTypSwitchEvent(algorithmEvent)) {
				this.handleNodeTypeSwitchEvent(algorithmEvent);
			} else if (this.correspondsToNodePropertySwitchEvent(algorithmEvent)) {
				this.handleNodePropertySwitchEvent(algorithmEvent);
			}
		} catch (ViewGraphManipulationException exception) {
			throw new HandleAlgorithmEventException("Encountered a problem while handling graph event " + algorithmEvent + " .", exception);
		}
	}

	private boolean correspondsToNodeTypSwitchEvent(final IPropertyProcessedAlgorithmEvent algorithmEvent) {
		return algorithmEvent.getEventName().equalsIgnoreCase(NodeTypeSwitchEvent.class.getSimpleName());
	}

	private boolean correspondsToNodePropertySwitchEvent(final IPropertyProcessedAlgorithmEvent algorithmEvent) {
		return algorithmEvent.getEventName().equalsIgnoreCase(NodePropertyChangedEvent.class.getSimpleName());
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
		this.getModel().addNode(nodeInfo.getMainNodeId(), Collections.emptyList(), "root");
	}

	private void handleNodeAddedEvent(final IPropertyProcessedAlgorithmEvent nodeReachedEvent) throws ViewGraphManipulationException {
		NodeInfo nodeInfo = nodeReachedEvent.getProperty(NodeInfoAlgorithmEventPropertyComputer.NODE_INFO_PROPERTY_NAME, NodeInfo.class);
		this.getModel().addNode(nodeInfo.getMainNodeId(), nodeInfo.getParentNodeIds().stream().map(s -> (Object) s).collect(Collectors.toList()), nodeInfo.getNodeType());
	}

	private void handleNodeTypeSwitchEvent(final IPropertyProcessedAlgorithmEvent nodeTypeSwitchEvent) throws ViewGraphManipulationException {
		NodeInfo nodeInfo = nodeTypeSwitchEvent.getProperty(NodeInfoAlgorithmEventPropertyComputer.NODE_INFO_PROPERTY_NAME, NodeInfo.class);
		this.getModel().switchNodeType(nodeInfo.getMainNodeId(), nodeInfo.getNodeType());
	}

	private void handleNodePropertySwitchEvent(final IPropertyProcessedAlgorithmEvent nodePropertySwitchEvent) throws ViewGraphManipulationException {
		NodeInfo nodeInfo = nodePropertySwitchEvent.getProperty(NodeInfoAlgorithmEventPropertyComputer.NODE_INFO_PROPERTY_NAME, NodeInfo.class);
		this.getModel().updateNodeProperties(nodeInfo.getMainNodeId(), nodeInfo.getProperties());
	}

	private void handleNodeRemovedEvent(final IPropertyProcessedAlgorithmEvent nodeRemovedEvent) throws ViewGraphManipulationException {
		NodeInfo nodeInfo = nodeRemovedEvent.getProperty(NodeInfoAlgorithmEventPropertyComputer.NODE_INFO_PROPERTY_NAME, NodeInfo.class);
		this.getModel().removeNode(nodeInfo.getMainNodeId());
	}

	@Override
	public void handleGUIEvent(final GUIEvent guiEvent) {
		if (guiEvent instanceof ResetEvent || guiEvent instanceof GoToTimeStepEvent) {
			this.getModel().clear();
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

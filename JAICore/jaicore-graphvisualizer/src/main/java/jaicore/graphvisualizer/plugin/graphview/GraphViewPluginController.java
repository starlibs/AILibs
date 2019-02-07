package jaicore.graphvisualizer.plugin.graphview;

import java.util.Arrays;
import java.util.Collections;

import jaicore.graphvisualizer.events.graph.GraphEvent;
import jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import jaicore.graphvisualizer.events.graph.NodeParentSwitchEvent;
import jaicore.graphvisualizer.events.graph.NodeRemovedEvent;
import jaicore.graphvisualizer.events.graph.NodeTypeSwitchEvent;
import jaicore.graphvisualizer.events.graph.bus.HandleGraphEventException;
import jaicore.graphvisualizer.events.gui.GUIEvent;
import jaicore.graphvisualizer.plugin.GUIPluginController;

public class GraphViewPluginController implements GUIPluginController {

	private GraphViewPluginModel model;

	public GraphViewPluginController(GraphViewPluginModel model) {
		this.model = model;
	}

	@Override
	public void handleGraphEvent(GraphEvent graphEvent) throws HandleGraphEventException {
		try {
			if (GraphInitializedEvent.class.isInstance(graphEvent)) {
				GraphInitializedEvent<?> graphInitializedEvent = (GraphInitializedEvent<?>) graphEvent;
				handleGraphInitializedEvent(graphInitializedEvent);
			} else if (NodeAddedEvent.class.isInstance(graphEvent)) {
				NodeAddedEvent<?> nodeAddedEvent = (NodeAddedEvent<?>) graphEvent;
				handleNodeAddedEvent(nodeAddedEvent);
			} else if (NodeParentSwitchEvent.class.isInstance(graphEvent)) {
				NodeParentSwitchEvent<?> nodeParentSwitchEvent = (NodeParentSwitchEvent<?>) graphEvent;
				handleNodeParentSwitchEvent(nodeParentSwitchEvent);
			} else if (NodeRemovedEvent.class.isInstance(graphEvent)) {
				NodeRemovedEvent<?> nodeRemovedEvent = (NodeRemovedEvent<?>) graphEvent;
				handleNodeRemovedEvent(nodeRemovedEvent);
			} else if (NodeTypeSwitchEvent.class.isInstance(graphEvent)) {
				NodeTypeSwitchEvent<?> nodeTypeSwitchEvent = (NodeTypeSwitchEvent<?>) graphEvent;
				handleNodeTypeSwitchEvent(nodeTypeSwitchEvent);
			}
		} catch (ViewGraphManipulationException exception) {
			throw new HandleGraphEventException("Encountered a problem while handling graph event " + graphEvent + " .", exception);
		}
	}

	private void handleGraphInitializedEvent(GraphInitializedEvent<?> graphInitializedEvent) throws ViewGraphManipulationException {
		model.addNode(graphInitializedEvent.getRoot(), Collections.emptyList(), "root");
	}

	private void handleNodeAddedEvent(NodeAddedEvent<?> nodeReachedEvent) throws ViewGraphManipulationException {
		model.addNode(nodeReachedEvent.getNode(), Arrays.asList(nodeReachedEvent.getParent()), nodeReachedEvent.getType());
	}

	private void handleNodeTypeSwitchEvent(NodeTypeSwitchEvent<?> nodeTypeSwitchEvent) throws ViewGraphManipulationException {
		model.switchNodeType(nodeTypeSwitchEvent.getNode(), nodeTypeSwitchEvent.getType());
	}

	private void handleNodeParentSwitchEvent(NodeParentSwitchEvent<?> nodeParentSwitchEvent) {
		// can be ignored currently as we have a tree search where this cannot happen
	}

	private void handleNodeRemovedEvent(NodeRemovedEvent<?> nodeRemovedEvent) throws ViewGraphManipulationException {
		model.removeNode(nodeRemovedEvent.getNode());
	}

	@Override
	public void handleGUIEvent(GUIEvent guiEvent) {
		// TODO Auto-generated method stub

	}

}

package jaicore.graphvisualizer.plugin.graphview;

import java.util.Arrays;
import java.util.Collections;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import jaicore.graphvisualizer.events.graph.NodeRemovedEvent;
import jaicore.graphvisualizer.events.graph.NodeTypeSwitchEvent;
import jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import jaicore.graphvisualizer.events.gui.GUIEvent;
import jaicore.graphvisualizer.plugin.IGUIPluginController;
import jaicore.graphvisualizer.plugin.controlbar.ResetEvent;
import jaicore.graphvisualizer.plugin.timeslider.GoToTimeStepEvent;

public class GraphViewPluginController implements IGUIPluginController {

	private GraphViewPluginModel model;

	public GraphViewPluginController(GraphViewPluginModel model) {
		this.model = model;
	}

	@Override
	public void handleAlgorithmEvent(AlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		try {
			if (GraphInitializedEvent.class.isInstance(algorithmEvent)) {
				GraphInitializedEvent<?> graphInitializedEvent = (GraphInitializedEvent<?>) algorithmEvent;
				handleGraphInitializedEvent(graphInitializedEvent);
			} else if (NodeAddedEvent.class.isInstance(algorithmEvent)) {
				NodeAddedEvent<?> nodeAddedEvent = (NodeAddedEvent<?>) algorithmEvent;
				handleNodeAddedEvent(nodeAddedEvent);
			} else if (NodeRemovedEvent.class.isInstance(algorithmEvent)) {
				NodeRemovedEvent<?> nodeRemovedEvent = (NodeRemovedEvent<?>) algorithmEvent;
				handleNodeRemovedEvent(nodeRemovedEvent);
			} else if (NodeTypeSwitchEvent.class.isInstance(algorithmEvent)) {
				NodeTypeSwitchEvent<?> nodeTypeSwitchEvent = (NodeTypeSwitchEvent<?>) algorithmEvent;
				handleNodeTypeSwitchEvent(nodeTypeSwitchEvent);
			}
		} catch (ViewGraphManipulationException exception) {
			throw new HandleAlgorithmEventException("Encountered a problem while handling graph event " + algorithmEvent + " .", exception);
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

	private void handleNodeRemovedEvent(NodeRemovedEvent<?> nodeRemovedEvent) throws ViewGraphManipulationException {
		model.removeNode(nodeRemovedEvent.getNode());
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

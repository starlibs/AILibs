package jaicore.graphvisualizer.gui.dataVisualizer;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.events.controlEvents.ResetEvent;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.gui.GraphVisualization;
import javafx.scene.Node;

/**
 * The NodeExpansionVisualizer is used to show the events, which happen on one
 * branch of the search tree. Usually the needed events, which are shown are
 * provided by jaicore.search.gui.dataSupplier.NodeExpansionSupplier.
 * 
 * @author jkoepe
 *
 */
public class NodeExpansionVisualizer implements IVisualizer {

	EventBus bus = new EventBus();
	GraphVisualization<?,?> visualization;

	/**
	 * Crates a new NodeExpansionVisualizer.
	 */
	public NodeExpansionVisualizer() {

		visualization = new GraphVisualization<>();

		bus.register(visualization);
	}

	@Override
	public String getSupplier() {
		return "NodeExpansionSupplier";
	}

	@Override
	public String getTitle() {
		return "NodeExpansion";
	}

	@Override
	public Node getVisualization() {
		return visualization.getFXNode();
	}

	@Subscribe
	public void receiveEvents(Object event) {
		try {
			if (event instanceof GraphInitializedEvent || event instanceof ResetEvent)
				visualization.reset();
			bus.post(event);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

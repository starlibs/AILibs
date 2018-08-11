package jaicore.graphvisualizer.guiOld.dataVisualizer;

import javax.swing.SwingUtilities;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.SearchVisualizationPanel;
import jaicore.graphvisualizer.events.controlEvents.ResetEvent;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
/**
 * The NodeExpansionVisualizer is used to show the events, which happen on one branch of the search tree.
 * Usually the needed events, which are shown are provided by jaicore.search.gui.dataSupplier.NodeExpansionSupplier. 
 * 
 * @author jkoepe
 *
 */
public class NodeExpansionVisualizer implements IVisualizer {
	
	SwingNode node;
	SearchVisualizationPanel<?> panel;
	EventBus bus = new EventBus();
	
	/**
	 * Crates a new NodeExpansionVisualizer.	
	 */
	public NodeExpansionVisualizer() {
		node = new SwingNode();
		
		 panel = new SearchVisualizationPanel();
		
		SwingUtilities.invokeLater(()->node.setContent(panel));
		
		bus.register(panel);
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
		return node;
	}
	
	@Subscribe
	public void receiveEvents(Object event) {
		if(event instanceof GraphInitializedEvent || event instanceof ResetEvent)
			panel.reset();
		bus.post(event);
	}
	
	/**
	 * Returns the SearchVisualizationPanel which is used to show the branch.
	 * @return
	 * 		The visualization panel
	 */
	public SearchVisualizationPanel<?> getPanel() {
		return this.panel;
	}

}

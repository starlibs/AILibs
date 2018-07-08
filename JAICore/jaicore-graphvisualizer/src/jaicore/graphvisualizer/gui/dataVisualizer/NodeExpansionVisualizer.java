package jaicore.graphvisualizer.gui.dataVisualizer;

import javax.swing.SwingUtilities;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.SearchVisualizationPanel;
import jaicore.graphvisualizer.events.GraphInitializedEvent;
import jaicore.graphvisualizer.events.controlEvents.ResetEvent;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;

public class NodeExpansionVisualizer implements IVisualizer {
	
	SwingNode node;
	SearchVisualizationPanel panel;
	EventBus bus = new EventBus();
	
	public NodeExpansionVisualizer() {
		node = new SwingNode();
		
		 panel = new SearchVisualizationPanel();
		
		SwingUtilities.invokeLater(()->node.setContent(panel));
		
		bus.register(panel);
	}

	@Override
	public String getSupplier() {
		// TODO Auto-generated method stub
		return "NodeExpansionSupplier";
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "NodeExpansion";
	}

	@Override
	public Node getVisualization() {
		// TODO Auto-generated method stub
		return node;
	}
	
	@Subscribe
	public void receiveEvents(Object event) {
		if(event instanceof GraphInitializedEvent || event instanceof ResetEvent)
			panel.reset();
		bus.post(event);
	}
	
	public SearchVisualizationPanel getPanel() {
		return this.panel;
	}

}

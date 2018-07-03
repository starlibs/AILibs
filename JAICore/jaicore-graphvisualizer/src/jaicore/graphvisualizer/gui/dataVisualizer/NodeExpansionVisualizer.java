package jaicore.graphvisualizer.gui.dataVisualizer;

import javax.swing.SwingUtilities;

import jaicore.graphvisualizer.SearchVisualizationPanel;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;

public class NodeExpansionVisualizer implements IVisualizer {
	
	SwingNode node;
	
	
	public NodeExpansionVisualizer() {
		node = new SwingNode();
		
		SearchVisualizationPanel panel = new SearchVisualizationPanel();
		
		SwingUtilities.invokeLater(()->node.setContent(panel));
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

}

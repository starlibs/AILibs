package jaicore.graphvisualizer;

import java.awt.Container;

import javax.swing.JFrame;

import jaicore.graph.IGraphAlgorithm;
import jaicore.graph.IGraphAlgorithmListener;

@SuppressWarnings("serial")
public class SimpleGraphVisualizationWindow<V,E> extends JFrame {
	
	private final SearchVisualizationPanel<V,E> panel;
	
	public SimpleGraphVisualizationWindow(IGraphAlgorithm<?, ?, V, E, IGraphAlgorithmListener<V,E>> graphAlgorithm) {
		super("Visualizer for " + graphAlgorithm);
		
		// initialize window data
		setSize(1600, 1000);
		setExtendedState(MAXIMIZED_BOTH);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		Container contentPane = this.getContentPane();
		contentPane.removeAll();

		/* create option panel */
		panel =  new SearchVisualizationPanel<>();
		graphAlgorithm.registerListener(panel);
		contentPane.add(panel);
		setVisible(true);
	}
	
	public SearchVisualizationPanel<V,E> getPanel() {
		return panel;
	}
}

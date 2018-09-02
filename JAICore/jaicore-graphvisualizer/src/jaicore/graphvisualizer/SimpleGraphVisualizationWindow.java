package jaicore.graphvisualizer;

import java.awt.Container;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JFrame;

import com.google.common.eventbus.EventBus;

import jaicore.graph.Graph;
import jaicore.graph.IObservableGraphAlgorithm;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;

@SuppressWarnings("serial")
public class SimpleGraphVisualizationWindow<T> extends JFrame {
	
	private final SearchVisualizationPanel<T> panel;
	
	public SimpleGraphVisualizationWindow(IObservableGraphAlgorithm observable) {
		super("Visualizer for " + observable);
		
		// initialize window data
		setSize(1600, 1000);
		setExtendedState(MAXIMIZED_BOTH);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		Container contentPane = this.getContentPane();
		contentPane.removeAll();

		/* create option panel */
		panel =  new SearchVisualizationPanel<>();
		observable.registerListener(panel);
		contentPane.add(panel);
		setVisible(true);
	}
	
	public SimpleGraphVisualizationWindow(Graph<T> graph) {
		super("Visualizer for a fixed graph");
		
		// initialize window data
		setSize(1600, 1000);
		setExtendedState(MAXIMIZED_BOTH);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		Container contentPane = this.getContentPane();
		contentPane.removeAll();

		/* create option panel */
		EventBus eventBus = new EventBus();
		panel =  new SearchVisualizationPanel<>();
		eventBus.register(panel);
		contentPane.add(panel);
		setVisible(true);
		
		/* now draw the graph */
		Queue<T> open = new LinkedList<>();
		for (T root : graph.getSources()) {
			open.add(root);
			eventBus.post(new GraphInitializedEvent<T>(root));
		}
		
		Collection<T> closed = new HashSet<>(); 
		while (!open.isEmpty()) {
			T next = open.poll();
			closed.add(next);
			for (T succ : graph.getSuccessors(next)) {
				if (!closed.contains(succ) && !open.contains(succ)) {
					open.add(succ);
				}
				eventBus.post(new NodeReachedEvent<T>(next, succ, "node"));
			}
		}
	}

	public SearchVisualizationPanel<T> getPanel() {
		return panel;
	}
}

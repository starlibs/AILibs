package jaicore.graphvisualizer.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.fx_viewer.util.FxMouseManager;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.graphstream.ui.view.util.InteractiveElement;

import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.events.controlEvents.EnableColouring;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeParentSwitchEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeRemovedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeTypeSwitchEvent;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

public class GraphVisualization<V,E> {

	protected Graph graph;
	protected FxViewer viewer;
	protected FxViewPanel viewPanel;

	protected int nodeCounter = 0;

	protected List<V> roots;

	protected final ConcurrentMap<V, Node> ext2intNodeMap = new ConcurrentHashMap<>();
	protected final ConcurrentMap<Node, V> int2extNodeMap = new ConcurrentHashMap<>();

	protected boolean loop = true;

	protected ViewerPipe pipe;
	Thread pipeThread;
	
	private ObjectEvaluator<V> evaluator;
	private boolean evaluation;
	
	private double bestValue;
	private double worstValue;
	
	private StackPane pane;
	private Rectangle gradient;
	
	private Label minLabel;
	private Label maxLabel;
	

	public GraphVisualization(ObjectEvaluator<V> evaluator) {
		this.evaluator = evaluator;
		this.roots = new ArrayList<>();
		this.graph = new SingleGraph("Search-Graph");
		this.bestValue = Double.MAX_VALUE;
		this.worstValue = -1;
		this.pane = new StackPane();
		pane.setAlignment(Pos.TOP_RIGHT);
	
		
		
		if(this.evaluator == null) {
			this.graph.setAttribute("ui.stylesheet", "url('conf/searchgraph.css')");

		}
		else {
			this.graph.setAttribute("ui.stylesheet", "url('conf/heatmap.css')");
			evaluation = true;
			this.gradient = this.createColorGradient();
			
		}
		try {
			this.viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
			this.viewer.enableAutoLayout();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		this.viewPanel = (FxViewPanel) viewer.addDefaultView(false);

		pipe = viewer.newViewerPipe();

		viewer.getDefaultView().setMouseManager(new FxMouseManager(
				EnumSet.of(InteractiveElement.EDGE, InteractiveElement.NODE, InteractiveElement.SPRITE)));

		pipeThread = new Thread() {
			@Override
			public void run() {
				loopPump();
			}
		};
		pane.getChildren().add(this.viewPanel);
		pipeThread.start();
		
		this.maxLabel = new Label();
		this.minLabel = new Label();
		if(evaluation) {
			this.pane.getChildren().add(gradient);
			Platform.runLater(()->{
				
			
			this.maxLabel.setTextFill(Color.CYAN);
			this.minLabel.setTextFill(Color.CYAN);
			pane.getChildren().add(this.maxLabel);

			this.minLabel.setTranslateY(485);
			pane.getChildren().add(this.minLabel);
			});
		}

	}

	public javafx.scene.Node getFXNode() {
		return pane;
	}
	
	@Subscribe
	public synchronized void receiveControlEvent(EnableColouring event) {
		this.evaluation = event.isColouring();
		toggleColouring(this.evaluation);
	}
	
	
	private void toggleColouring(boolean colouring) {
		if(colouring) {
			this.graph.clearAttributes();
			this.graph.setAttribute("ui.stylesheet", "url('conf/heatmap.css')");
			gradient = createColorGradient();
			pane.getChildren().add(gradient);
			Platform.runLater(()->{
				this.maxLabel.setTextFill(Color.CYAN);
				this.minLabel.setTextFill(Color.CYAN);
				pane.getChildren().add(this.maxLabel);

				this.minLabel.setTranslateY(485);
				pane.getChildren().add(this.minLabel);
			});
			
			
			update();
		}
		else {
			this.graph.setAttribute("ui.stylesheet", "url('conf/searchgraph.css')");
			pane.getChildren().remove(this.gradient);
			pane.getChildren().remove(this.maxLabel);
			pane.getChildren().remove(this.minLabel);
			
		}
		update();
		
	}
	
	@Subscribe
	public synchronized void receiveGraphInitEvent(GraphInitializedEvent<V> e) {
		try {
			roots.add(e.getRoot());
			if (roots == null)
				throw new IllegalArgumentException("Root must not be NULL");
			newNode(roots.get(roots.size() - 1));
			ext2intNodeMap.get(roots.get(roots.size() - 1)).setAttribute("ui.class", "root");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Subscribe
	public synchronized void receiveNewNodeEvent(NodeReachedEvent<V> e) {
		try {
			if (!ext2intNodeMap.containsKey(e.getNode()))
				newNode(e.getNode());
			newEdge(e.getParent(), e.getNode());
			ext2intNodeMap.get(e.getNode()).setAttribute("ui.class", e.getType());
		} catch (Exception ex) {

		}
	}

	@Subscribe
	public synchronized void receiveNodeTypeSwitchEvent(NodeTypeSwitchEvent<V> e) {
		try {
			if (roots.contains(e.getNode()))
				return;
			if (!ext2intNodeMap.containsKey(e.getNode()))
				throw new NoSuchElementException(
						"Cannot switch type of node " + e.getNode() + ". This node has not been reached previously.");
			ext2intNodeMap.get(e.getNode()).setAttribute("ui.class", e.getType());
		} catch (Exception ex) {

		}
	}

	@Subscribe
	public synchronized void receivedNodeParentSwitchEvent(NodeParentSwitchEvent<V> e) {
		try {
			if (!ext2intNodeMap.containsKey(e.getNode()) && !ext2intNodeMap.containsKey(e.getNewParent()))
				throw new NoSuchElementException("Cannot switch parent of node " + e.getNode()
						+ ". Either the node or the new parent node has not been reached previously.");

			removeEdge(e.getOldParent(), e.getNode());
			newEdge(e.getNewParent(), e.getNode());

		} catch (Exception ex) {

		}
	}

	@Subscribe
	public synchronized void receiveNodeRemovedEvent(NodeRemovedEvent<V> e) {
		try {
			graph.removeNode(ext2intNodeMap.get(e.getNode()));
			ext2intNodeMap.remove(e.getNode());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Creation of a new node in the graph
	 * 
	 * @param newNodeExt The external representation of the node
	 * @return The internal representation of the node
	 */
	protected synchronized Node newNode(final V newNodeExt) {

		/* create new node */
		final String nodeId = "n" + (nodeCounter++);
		if (this.ext2intNodeMap.containsKey(newNodeExt) || this.graph.getNode(nodeId) != null) {
			throw new IllegalArgumentException("Cannot insert node " + newNodeExt + " because it is already known.");
		}
		final Node newNodeInt = this.graph.addNode(nodeId);

		/*
		 * store relation between node in graph and internal representation of the node
		 */
		this.ext2intNodeMap.put(newNodeExt, newNodeInt);
		this.int2extNodeMap.put(newNodeInt, newNodeExt);
		
		
		/*
		 * comnpute fvalue if possible
		 */
		if(evaluator != null) {
			try {
				double value = evaluator.evaluate(newNodeExt);
				if(value < bestValue) {
					this.bestValue = value;
					Platform.runLater(()->{
						this.minLabel.setText(Double.toString(bestValue));
					});
					
				}
				if(value > worstValue) {
					this.worstValue = value;
					Platform.runLater(()->{
						this.maxLabel.setText(Double.toString(this.worstValue));
					});
					
				}
				
				if(!roots.contains(newNodeExt))
					colourNode(newNodeInt, value);
				
			}
			catch(Exception e) {
				
			}
		}

		/* store relation between node an parent in internal model */
		return newNodeInt;
	}

	/**
	 * Creation of a new edge in the graph
	 * 
	 * @param from The source of the edge
	 * @param to   The endpoint of the edge
	 * @return The new edge
	 */
	protected synchronized Edge newEdge(final V from, final V to) {
		final Node fromInt = this.ext2intNodeMap.get(from);
		final Node toInt = this.ext2intNodeMap.get(to);
		if (fromInt == null)
			throw new IllegalArgumentException(
					"Cannot insert edge between " + from + " and " + to + " since node " + from + " does not exist.");
		if (toInt == null)
			throw new IllegalArgumentException(
					"Cannot insert edge between " + from + " and " + to + " since node " + to + " does not exist.");
		final String edgeId = fromInt.getId() + "-" + toInt.getId();
		return this.graph.addEdge(edgeId, fromInt, toInt, false);
	}

	protected synchronized boolean removeEdge(final V from, final V to) {
//		for(Edge e: graph.getEachEdge()) {
//			if (e.getSourceNode().equals(this.ext2intNodeMap.get(from)) && e.getTargetNode().equals(this.ext2intNodeMap.get(to))) {
//				graph.removeEdge(e);
//				return true;
//			}
//		}
		return false;
	}

	/**
	 * Resets the visualization of the graph
	 */
	public void reset() {
		this.ext2intNodeMap.clear();
		this.int2extNodeMap.clear();
		this.nodeCounter = 0;
		this.graph.clear();
		if(this.evaluator == null) {
			this.graph.setAttribute("ui.stylesheet", "url('conf/searchgraph.css')");
		}
		else {
			this.graph.setAttribute("ui.stylesheet", "url('conf/heatmap.css')");
			this.gradient = this.createColorGradient();
		}
	}

	/**
	 * Used to enable node pushing etc.
	 */
	private void loopPump() {
		while (loop) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			pipe.pump();
		}
	}

	private V getNodeOfString(String name) {
		return int2extNodeMap.get(graph.getNode(name));
	}

	public void addNodeListener(NodeListener<V> listener) {
		this.pipe.addViewerListener(new ViewerListener() {
			@Override
			public void viewClosed(String id) {

			}

			@Override
			public void buttonPushed(String id) {
				listener.buttonPushed(getNodeOfString(id));
			}

			@Override
			public void buttonReleased(String id) {
				listener.buttonReleased(getNodeOfString(id));

			}

			@Override
			public void mouseOver(String id) {
				listener.mouseOver(getNodeOfString(id));

			}

			@Override
			public void mouseLeft(String id) {
				listener.mouseLeft(getNodeOfString(id));
			}
		});
	}
	
	private void colourNode(Node node, double value) {
		float color = 1;
		float x = (float) (value -bestValue);
		float y = (float)(worstValue - bestValue);
		color = x/y;
		if(Float.isNaN(color)) {
			color = 1;
		}
		if(evaluation) {
			node.setAttribute("ui.color", color);
		}
	}
	
	public void update() {
		for(V n: this.ext2intNodeMap.keySet()) {
			
			double value = evaluator.evaluate(n);
			colourNode(ext2intNodeMap.get(n), value);
		}
		
	}
	
	
	/**
	 * Create the color gradient
	 * @return Creates the color gradient
	 */
	protected Rectangle createColorGradient() {

		Rectangle box = new Rectangle(50, 500);
		Stop[] stops = new Stop[] { new Stop(0, Color.BLUE), new Stop(1, Color.RED) };
		ArrayList<Stop> list = new ArrayList<Stop>();

		try {
			Files.lines(Paths.get("conf/heatmap.css"))
					.filter(line -> line.contains("fill-color"))

					.filter(line -> !line.contains("/*")).forEach(line -> {
						String s = line.replace("fill-color: ", "").replace(";", "").replace(" ", "");
						String[] a = s.split(",");
						if (a.length > 1) {
							double d = 1.0 / (a.length - 1);
							for (int i = 0; i < a.length; i++) {
								list.add(new Stop(d * i, Color.web(a[i].trim())));
							}
						}

					});
		} catch (IOException e) {
			e.printStackTrace();
		}
		stops = list.toArray(new Stop[0]);
		LinearGradient lg = new LinearGradient(0, 1, 0, 0, true, CycleMethod.NO_CYCLE, stops);
		box.setFill(lg);
		this.gradient = box;
		return box;
	}
}

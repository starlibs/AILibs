package ai.libs.jaicore.graphvisualizer.plugin.graphview;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.ResourceUtil;
import ai.libs.jaicore.graphvisualizer.IColorMap;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginModel;

public class GraphViewPluginModel extends ASimpleMVCPluginModel<GraphViewPluginView, GraphViewPluginController> {

	private Logger logger = LoggerFactory.getLogger(GraphViewPluginModel.class);

	private static final String L_UI_CLASS = "ui.class";

	private static final String DEFAULT_RESSOURCE_STYLESHEET_PATH = "searchgraph.css";
	private static final String STYLESHEET_PATH = "conf/searchgraph.css";

	private static final File DEFAULT_STYLESHEET_FILE = FileUtil.getExistingFileWithHighestPriority(DEFAULT_RESSOURCE_STYLESHEET_PATH, STYLESHEET_PATH);

	private final AtomicInteger nodeIdCounter = new AtomicInteger(0);

	private Graph graph;
	private final ConcurrentMap<String, Node> searchGraphNodesToViewGraphNodesMap;
	private final ConcurrentMap<Node, String> viewGraphNodesToSearchGraphNodesMap;

	private final ConcurrentMap<Node, Set<Edge>> nodeToConnectedEdgesMap;

	private final ConcurrentMap<Node, Map<String, Object>> nodeProperties;

	/* node color scheme */
	private double nodeColoringMin;
	private double nodeColoringMax;
	private IColorMap colormap;
	private String propertyToUseForColoring;

	private boolean withPropertyLabels = false;

	public GraphViewPluginModel() {
		this(DEFAULT_STYLESHEET_FILE);
	}

	private GraphViewPluginModel(final File searchGraphCSSPath) {
		this.searchGraphNodesToViewGraphNodesMap = new ConcurrentHashMap<>();
		this.viewGraphNodesToSearchGraphNodesMap = new ConcurrentHashMap<>();
		this.nodeToConnectedEdgesMap = new ConcurrentHashMap<>();
		this.nodeProperties = new ConcurrentHashMap<>();

		this.initializeGraph(searchGraphCSSPath);
	}

	private void initializeGraph(final File searchGraphCSSPath) {
		this.graph = new SingleGraph("Search Graph");

		try {
			this.graph.setAttribute("ui.stylesheet", FileUtil.readFileAsString(searchGraphCSSPath.getPath()));
		} catch (IOException e) {
			this.logger.warn("Could not load css stylesheet for graph view plugin. Continue without stylesheet", e);
		}
	}

	public synchronized void addNode(final String node, final List<Object> predecessorNodes, final String typeOfNode) throws ViewGraphManipulationException {
		try {
			this.logger.debug("Adding node with external id {}", node);
			Node viewNode = this.graph.addNode(String.valueOf(this.nodeIdCounter.getAndIncrement()));
			this.registerNodeMapping(node, viewNode);

			for (Object predecessorNode : predecessorNodes) {
				this.createEdge(node, predecessorNode);
			}
			this.switchNodeType(viewNode, typeOfNode);
			this.getView().update();
			this.logger.debug("Added node with external id {}. Internal id is {}", node, viewNode.getId());
		} catch (IdAlreadyInUseException exception) {
			throw new ViewGraphManipulationException("Cannot add node " + node + " as the id " + this.nodeIdCounter + " is already in use.");
		}
	}

	private synchronized void createEdge(final Object node, final Object predecessorNode) throws ViewGraphManipulationException {
		Node viewNode = this.searchGraphNodesToViewGraphNodesMap.get(node);
		Node viewPredecessorNode = this.searchGraphNodesToViewGraphNodesMap.get(predecessorNode);
		if (viewPredecessorNode == null) {
			throw new ViewGraphManipulationException("Cannot add edge from node " + predecessorNode + " to node " + viewNode + " due to missing view node of predecessor.");
		}
		String edgeId = viewPredecessorNode.getId() + "-" + viewNode.getId();
		Edge edge = this.graph.addEdge(edgeId, viewPredecessorNode, viewNode, true);
		this.registerEdgeConnectedToNodesInMap(edge);
	}

	private void registerNodeMapping(final String node, final Node viewNode) {
		this.searchGraphNodesToViewGraphNodesMap.put(node, viewNode);
		this.viewGraphNodesToSearchGraphNodesMap.put(viewNode, node);
	}

	private void registerEdgeConnectedToNodesInMap(final Edge edge) {
		if (!this.nodeToConnectedEdgesMap.containsKey(edge.getNode0())) {
			this.nodeToConnectedEdgesMap.put(edge.getNode0(), new HashSet<>());
		}
		this.nodeToConnectedEdgesMap.get(edge.getNode0()).add(edge);

		if (!this.nodeToConnectedEdgesMap.containsKey(edge.getNode1())) {
			this.nodeToConnectedEdgesMap.put(edge.getNode1(), new HashSet<>());
		}
		this.nodeToConnectedEdgesMap.get(edge.getNode1()).add(edge);
	}

	public void switchNodeType(final Object node, final String newType) throws ViewGraphManipulationException {
		if (node == null) {
			throw new ViewGraphManipulationException("Cannot switch type of null node.");
		}
		Node viewNode = this.searchGraphNodesToViewGraphNodesMap.get(node);
		if (viewNode == null) {
			throw new ViewGraphManipulationException("Cannot switch type of node " + node + " without corresponding view node.");
		}
		this.switchNodeType(viewNode, newType);
		this.getView().update();
	}

	private void switchNodeType(final Node node, final String newType) {
		if (!this.isLabeledAsRootNode(node)) {
			node.setAttribute(L_UI_CLASS, newType);
		}
	}

	public void updateNodeProperties(final Object node, final Map<String, Object> properties) throws ViewGraphManipulationException {
		if (node == null) {
			throw new ViewGraphManipulationException("Cannot switch type of null node.");
		}
		Node viewNode = this.searchGraphNodesToViewGraphNodesMap.get(node);
		this.updateNodeProperties(viewNode, properties);
	}

	public void updateNodeProperties(final Node node, final Map<String, Object> properties) {
		this.nodeProperties.computeIfAbsent(node, n -> new HashMap<>()).putAll(properties);
		if (properties.containsKey(this.propertyToUseForColoring)) {
			Object valAsObject = properties.get(this.propertyToUseForColoring);
			double val = valAsObject instanceof Double ? (double) valAsObject : Double.valueOf("" + valAsObject);
			Color c = this.colormap.get(this.nodeColoringMin, this.nodeColoringMax, val);
			node.setAttribute("ui.style", "fill-color: rgb(" + c.getRed() + "," + c.getGreen() + ", " + c.getBlue() + ");");
			if (this.withPropertyLabels) {
				node.setAttribute("ui.label", this.nodeProperties.get(node).entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining("\n")));
			}
		}
	}

	private boolean isLabeledAsRootNode(final Node node) {
		return node.getAttribute(L_UI_CLASS) != null && node.getAttribute(L_UI_CLASS).equals("root");
	}

	public void removeNode(final Object node) throws ViewGraphManipulationException {
		if (node == null) {
			throw new ViewGraphManipulationException("Cannot remove null node.");
		}
		Node viewNode = this.searchGraphNodesToViewGraphNodesMap.remove(node);
		if (viewNode == null) {
			throw new ViewGraphManipulationException("Cannot remove node " + node + " without corresponding view node.");
		}
		this.viewGraphNodesToSearchGraphNodesMap.remove(viewNode);

		Set<Edge> connectedEdges = this.nodeToConnectedEdgesMap.remove(viewNode);
		this.graph.removeNode(viewNode);
		for (Edge edge : connectedEdges) {
			Node otherNode = edge.getNode0().equals(viewNode) ? edge.getNode1() : edge.getNode0();
			Set<Edge> connectedEdgesOfOtherNode = this.nodeToConnectedEdgesMap.get(otherNode);
			connectedEdgesOfOtherNode.remove(edge);
		}
	}

	public Graph getGraph() {
		return this.graph;
	}

	public String getSearchGraphNodeMappedToViewGraphNode(final Object viewGraphNode) {
		return this.viewGraphNodesToSearchGraphNodesMap.get(viewGraphNode);
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}

	@Override
	public void clear() {
		this.graph.clear();
		try {
			this.graph.setAttribute("ui.stylesheet", ResourceUtil.readResourceFileToString(DEFAULT_STYLESHEET_FILE.getPath()));
		} catch (IOException e) {
			this.logger.warn("Could not load css stylesheet for graph view plugin. Continue without stylesheet", e);
		}
		this.searchGraphNodesToViewGraphNodesMap.clear();
		this.viewGraphNodesToSearchGraphNodesMap.clear();
		this.nodeToConnectedEdgesMap.clear();
		this.getView().update();
	}

	public void setPropertyBasedNodeColoring(final String propertyName, final IColorMap colorScheme, final double min, final double max) {
		this.propertyToUseForColoring = propertyName;
		this.colormap = colorScheme;
		this.nodeColoringMin = min;
		this.nodeColoringMax = max;
	}

	public boolean isWithPropertyLabels() {
		return this.withPropertyLabels;
	}

	public void setWithPropertyLabels(final boolean withPropertyLabels) {
		this.withPropertyLabels = withPropertyLabels;
	}
}

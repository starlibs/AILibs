package ai.libs.jaicore.graphvisualizer.plugin.graphview;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.api4.java.common.control.ILoggingCustomizable;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.ResourceUtil;
import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginModel;

public class GraphViewPluginModel implements IGUIPluginModel, ILoggingCustomizable {

	private Logger logger= LoggerFactory.getLogger(GraphViewPluginModel.class);

	private static final String DEF_RES_STYLESHEET_PATH = "searchgraph.css";
	private static final String STYLESHEET_PATH = "conf/searchgraph.css";

	private static final File DEF_STYLESHEET = FileUtil.getExistingFileWithHighestPriority(DEF_RES_STYLESHEET_PATH, STYLESHEET_PATH);

	private final AtomicInteger nodeIdCounter = new AtomicInteger(0);

	private GraphViewPluginView view;

	private Graph graph;
	private ConcurrentMap<String, Node> searchGraphNodesToViewGraphNodesMap;
	private ConcurrentMap<Node, String> viewGraphNodesToSearchGraphNodesMap;

	private ConcurrentMap<Node, Set<Edge>> nodeToConnectedEdgesMap;

	public GraphViewPluginModel(final GraphViewPluginView view) {
		this(view, DEF_STYLESHEET);
	}

	public GraphViewPluginModel(final GraphViewPluginView view, final File searchGraphCSSPath) {
		this.view = view;
		this.searchGraphNodesToViewGraphNodesMap = new ConcurrentHashMap<>();
		this.viewGraphNodesToSearchGraphNodesMap = new ConcurrentHashMap<>();
		this.nodeToConnectedEdgesMap = new ConcurrentHashMap<>();

		this.initializeGraph(searchGraphCSSPath);
	}

	private void initializeGraph(final File searchGraphCSSPath) {
		this.graph = new SingleGraph("Search Graph");

		try {
			this.graph.setAttribute("ui.stylesheet", ResourceUtil.readResourceFileToString(searchGraphCSSPath.getPath()));
		} catch (IOException e) {
			this.logger.warn("Could not load css stylesheet for graph view plugin. Continue without stylesheet", e);
		}
	}

	public synchronized void addNode(final String node, final List<Object> predecessorNodes, final String typeOfNode) throws ViewGraphManipulationException {
		try  {
			this.logger.debug("Adding node with external id {}", node);
			Node viewNode = this.graph.addNode(String.valueOf(this.nodeIdCounter.getAndIncrement()));
			this.registerNodeMapping(node, viewNode);

			for (Object predecessorNode : predecessorNodes) {
				this.createEdge(node, predecessorNode);
			}
			this.switchNodeType(viewNode, typeOfNode);
			this.view.update();
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
		this.view.update();
	}

	private void switchNodeType(final Node node, final String newType) {
		if (!this.isLabeledAsRootNode(node)) {
			node.setAttribute("ui.class", newType);
		}
	}

	private boolean isLabeledAsRootNode(final Node node) {
		return node.getAttribute("ui.class") != null && node.getAttribute("ui.class").equals("root");
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
			this.graph.removeEdge(edge);
		}
	}

	public void reset() {
		this.graph.clear();
		this.graph.setAttribute("ui.stylesheet", "url('conf/searchgraph.css')");
		this.searchGraphNodesToViewGraphNodesMap.clear();
		this.viewGraphNodesToSearchGraphNodesMap.clear();
		this.nodeToConnectedEdgesMap.clear();
		this.view.update();
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
}

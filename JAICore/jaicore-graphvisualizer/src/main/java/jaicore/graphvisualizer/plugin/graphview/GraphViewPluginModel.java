package jaicore.graphvisualizer.plugin.graphview;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import jaicore.graphvisualizer.plugin.IGUIPluginModel;

public class GraphViewPluginModel implements IGUIPluginModel {

	private int nodeIdCounter;

	private GraphViewPluginView view;

	private Graph graph;
	private ConcurrentMap<Object, Node> searchGraphNodesToViewGraphNodesMap;
	private ConcurrentMap<Node, Object> viewGraphNodesToSearchGraphNodesMap;

	private ConcurrentMap<Node, Set<Edge>> nodeToConnectedEdgesMap;

	public GraphViewPluginModel(GraphViewPluginView view) {
		this.view = view;
		this.searchGraphNodesToViewGraphNodesMap = new ConcurrentHashMap<>();
		this.viewGraphNodesToSearchGraphNodesMap = new ConcurrentHashMap<>();
		this.nodeToConnectedEdgesMap = new ConcurrentHashMap<>();
		this.nodeIdCounter = 0;

		initializeGraph();
	}

	private void initializeGraph() {
		this.graph = new SingleGraph("Search Graph");
		this.graph.setAttribute("ui.stylesheet", "url('conf/searchgraph.css')");
	}

	public void addNode(Object node, List<Object> predecessorNodes, String typeOfNode) throws ViewGraphManipulationException {
		try {
			Node viewNode = graph.addNode(String.valueOf(nodeIdCounter));
			registerNodeMapping(node, viewNode);

			for (Object predecessorNode : predecessorNodes) {
				createEdge(viewNode, predecessorNode);
			}

			switchNodeType(viewNode, typeOfNode);
			view.update();
			nodeIdCounter++;
		} catch (IdAlreadyInUseException exception) {
			throw new ViewGraphManipulationException("Cannot add node " + node + " as the id " + nodeIdCounter + " is already in use.");
		}
	}

	private void createEdge(Object node, Object predecessorNode) throws ViewGraphManipulationException {
		Node viewNode = searchGraphNodesToViewGraphNodesMap.get(node);
		Node viewPredecessorNode = searchGraphNodesToViewGraphNodesMap.get(predecessorNode);
		if (viewPredecessorNode == null) {
			throw new ViewGraphManipulationException("Cannot add edge from node " + predecessorNode + " to node " + viewNode + " due to missing view node of predecessor.");
		}
		String edgeId = viewPredecessorNode.getId() + "-" + viewNode.getId();
		Edge edge = graph.addEdge(edgeId, viewPredecessorNode, viewNode, true);
		registerEdgeConnectedToNodesInMap(edge);
	}

	private void registerNodeMapping(Object node, Node viewNode) {
		searchGraphNodesToViewGraphNodesMap.put(node, viewNode);
		viewGraphNodesToSearchGraphNodesMap.put(viewNode, node);
	}

	private void registerEdgeConnectedToNodesInMap(Edge edge) {
		if (!nodeToConnectedEdgesMap.containsKey(edge.getNode0())) {
			nodeToConnectedEdgesMap.put(edge.getNode0(), new HashSet<>());
		}
		nodeToConnectedEdgesMap.get(edge.getNode0()).add(edge);

		if (!nodeToConnectedEdgesMap.containsKey(edge.getNode1())) {
			nodeToConnectedEdgesMap.put(edge.getNode1(), new HashSet<>());
		}
		nodeToConnectedEdgesMap.get(edge.getNode1()).add(edge);
	}

	public void switchNodeType(Object node, String newType) throws ViewGraphManipulationException {
		if (node == null) {
			throw new ViewGraphManipulationException("Cannot switch type of null node.");
		}
		Node viewNode = searchGraphNodesToViewGraphNodesMap.get(node);
		if (viewNode == null) {
			throw new ViewGraphManipulationException("Cannot switch type of node " + node + " without corresponding view node.");
		}
		switchNodeType(viewNode, newType);
		view.update();
	}

	private void switchNodeType(Node node, String newType) {
		if (!isLabeledAsRootNode(node)) {
			node.setAttribute("ui.class", newType);
		}
	}

	private boolean isLabeledAsRootNode(Node node) {
		return node.getAttribute("ui.class") != null && node.getAttribute("ui.class").equals("root");
	}

	public void removeNode(Object node) throws ViewGraphManipulationException {
		if (node == null) {
			throw new ViewGraphManipulationException("Cannot remove null node.");
		}
		Node viewNode = searchGraphNodesToViewGraphNodesMap.remove(node);
		if (viewNode == null) {
			throw new ViewGraphManipulationException("Cannot remove node " + node + " without corresponding view node.");
		}
		viewGraphNodesToSearchGraphNodesMap.remove(viewNode);

		Set<Edge> connectedEdges = nodeToConnectedEdgesMap.remove(viewNode);
		graph.removeNode(viewNode);
		for (Edge edge : connectedEdges) {
			Node otherNode = edge.getNode0().equals(viewNode) ? edge.getNode1() : edge.getNode0();
			Set<Edge> connectedEdgesOfOtherNode = nodeToConnectedEdgesMap.get(otherNode);
			connectedEdgesOfOtherNode.remove(edge);
			graph.removeEdge(edge);
		}
	}

	public void reset() {
		graph.clear();
		graph.setAttribute("ui.stylesheet", "url('conf/searchgraph.css')");
		searchGraphNodesToViewGraphNodesMap.clear();
		viewGraphNodesToSearchGraphNodesMap.clear();
		nodeToConnectedEdgesMap.clear();
		view.update();
	}

	public Graph getGraph() {
		return graph;
	}

	public Object getSearchGraphNodeMappedToViewGraphNode(Object searchGraphNode) {
		return viewGraphNodesToSearchGraphNodesMap.get(searchGraphNode);
	}
}

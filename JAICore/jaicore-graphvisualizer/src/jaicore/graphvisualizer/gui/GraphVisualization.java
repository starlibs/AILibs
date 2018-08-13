package jaicore.graphvisualizer.gui;

import com.google.common.eventbus.Subscribe;
import jaicore.graphvisualizer.events.graphEvents.*;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GraphVisualization<T> {

    protected Graph graph;
    protected FxViewer viewer;
    protected FxViewPanel viewPanel;

    protected int nodeCounter = 0;

    protected List<T> roots;

    protected final ConcurrentMap<T, Node> ext2intNodeMap = new ConcurrentHashMap<>();
    protected final ConcurrentMap<Node, T> int2extNodeMap = new ConcurrentHashMap<>();

    public GraphVisualization(){
        this.roots = new ArrayList<>();
        this.graph = new SingleGraph("Search-Graph");
        this.graph.setAttribute("ui.stylesheet", "url('conf/searchgraph.css')");
        this.viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        this.viewer.enableAutoLayout();


        this.viewPanel = (FxViewPanel) viewer.addDefaultView(false);



    }

    public FxViewPanel getViewPanel() {
        return viewPanel;
    }

    @Subscribe
    public synchronized void receiveGraphInitEvent(GraphInitializedEvent<T> e) {
        try {

//			if (roots != null)
//				throw new UnsupportedOperationException("Cannot initialize the graph for a second time!");
            roots.add(e.getRoot());
            if (roots == null)
                throw new IllegalArgumentException("Root must not be NULL");
            newNode(roots.get(roots.size()-1));
			ext2intNodeMap.get(roots.get(roots.size()-1)).setAttribute("ui.class", "root");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Subscribe
    public synchronized void receiveNewNodeEvent(NodeReachedEvent<T> e) {
        try {
            if (!ext2intNodeMap.containsKey(e.getNode()))
                newNode(e.getNode());
            newEdge(e.getParent(), e.getNode());
			ext2intNodeMap.get(e.getNode()).setAttribute("ui.class", e.getType());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Subscribe
    public synchronized void receiveNodeTypeSwitchEvent(NodeTypeSwitchEvent<T> e) {
        try {
            if (roots.contains(e.getNode()))
                return;
            if (!ext2intNodeMap.containsKey(e.getNode()))
                throw new NoSuchElementException("Cannot switch type of node " + e.getNode() + ". This node has not been reached previously.");
			ext2intNodeMap.get(e.getNode()).setAttribute("ui.class", e.getType());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Subscribe
    public synchronized void receivedNodeParentSwitchEvent(NodeParentSwitchEvent<T> e) {
        try {
            if(!ext2intNodeMap.containsKey(e.getNode()) && !ext2intNodeMap.containsKey(e.getNewParent()))
                throw new NoSuchElementException("Cannot switch parent of node " + e.getNode()+". Either the node or the new parent node has not been reached previously.");

            removeEdge(e.getOldParent(), e.getNode());
            newEdge(e.getNewParent(),e.getNode());

        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    @Subscribe
    public synchronized void receiveNodeRemovedEvent(NodeRemovedEvent<T> e) {
        try {
            graph.removeNode(ext2intNodeMap.get(e.getNode()));
//             ext2intNodeMap.get(e.getNode()).setAttribute("ui.class", e.getType());
            ext2intNodeMap.remove(e.getNode());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected synchronized Node newNode(final T newNodeExt) {

        /* create new node */
        final String nodeId = "n" + (nodeCounter++);
        if (this.ext2intNodeMap.containsKey(newNodeExt) || this.graph.getNode(nodeId) != null) {
            throw new IllegalArgumentException("Cannot insert node " + newNodeExt + " because it is already known.");
        }
        final Node newNodeInt = this.graph.addNode(nodeId);

        /* store relation between node in graph and internal representation of the node */
        this.ext2intNodeMap.put(newNodeExt, newNodeInt);
        this.int2extNodeMap.put(newNodeInt, newNodeExt);

        /* store relation between node an parent in internal model */
        return newNodeInt;
    }

    protected synchronized Edge newEdge(final T from, final T to) {
        final Node fromInt = this.ext2intNodeMap.get(from);
        final Node toInt = this.ext2intNodeMap.get(to);
        if (fromInt == null)
            throw new IllegalArgumentException("Cannot insert edge between " + from + " and " + to + " since node " + from + " does not exist.");
        if (toInt == null)
            throw new IllegalArgumentException("Cannot insert edge between " + from + " and " + to + " since node " + to + " does not exist.");
        final String edgeId = fromInt.getId() + "-" + toInt.getId();
//TODO		return this.graph.addEdge(edgeId, fromInt, toInt, true);
        return this.graph.addEdge(edgeId, fromInt, toInt, false);
    }

    protected synchronized boolean removeEdge(final T from, final T to) {
//		for(Edge e: graph.getEachEdge()) {
//			if (e.getSourceNode().equals(this.ext2intNodeMap.get(from)) && e.getTargetNode().equals(this.ext2intNodeMap.get(to))) {
//				graph.removeEdge(e);
//				return true;
//			}
//		}
        return false;
    }

    public void reset(){
        this.ext2intNodeMap.clear();
        this.int2extNodeMap.clear();
        this.graph.clear();
        this.graph.setAttribute("ui.stylesheet", "url('conf/searchgraph.css')");
    }
}

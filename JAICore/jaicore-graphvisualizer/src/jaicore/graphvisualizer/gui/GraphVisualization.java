package jaicore.graphvisualizer.gui;

import com.google.common.eventbus.Subscribe;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.graph.Graph;
import org.graphstream.ui.view.Viewer;

public class GraphVisualization {

    private Graph graph;
    private FxViewer viewer;
    private FxViewPanel viewPanel;

    public GraphVisualization(){
        this.graph = new SingleGraph("Search-Graph");
        this.viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        this.viewer.enableAutoLayout();


        this.viewPanel = (FxViewPanel) viewer.addDefaultView(false);
        newNode();
    }

    public FxViewPanel getViewPanel() {
        return viewPanel;
    }

    public Node newNode(){
        final String nodeId = "n";
        final Node newNodeInt = this.graph.addNode(nodeId);
        return newNodeInt;
    }
}

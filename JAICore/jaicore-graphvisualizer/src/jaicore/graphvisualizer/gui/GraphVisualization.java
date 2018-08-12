package jaicore.graphvisualizer.gui;

import com.google.common.eventbus.Subscribe;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GraphVisualization<T> {

    private Graph graph;
    private FxViewer viewer;
    private FxViewPanel viewPanel;

    private int nodeCounter = 0;

    private List<T> roots;

    private final ConcurrentMap<T, Node> ext2intNodeMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<T, Node> int2extNodeMap = new ConcurrentHashMap<>();

    public GraphVisualization(){
        this.graph = new SingleGraph("Search-Graph");
        this.viewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        this.viewer.enableAutoLayout();


        this.viewPanel = (FxViewPanel) viewer.addDefaultView(false);

//        this.graph.setAttribute("ui.stylesheet", "url(conf/searchgraph.ccs");

    }

    public FxViewPanel getViewPanel() {
        return viewPanel;
    }

    public Node newNode(){
        String nodeid = "n";
        Node node = this.graph.addNode(nodeid);
        node.setAttribute("ui.stylesheet", "node {\n" +
                "\tsize-mode: fit;\n" +
                "\tfill-mode: plain;\n" +
                "\tfill-color: blue;\n" +
                "\tpadding: 5px;\n" +
                "}");
        return node;
    }

    @Subscribe
    public void receiveGraphInit(GraphInitializedEvent e){
        newNode();
    }
}

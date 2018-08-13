package jaicore.graphvisualizer.gui;

import com.google.common.eventbus.Subscribe;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import org.graphstream.graph.Node;

public class HeatVisualization<T> extends GraphVisualization<T> {

    public HeatVisualization() {
        super();
        this.graph.clear();
    }

    @Override
    protected synchronized Node newNode(final T newNodeExt) {
        Node node = super.newNode(newNodeExt);
        System.out.println("test");
        node.setAttribute("ui.style", "fill-color : blue;");
        return node;

    }

    @Override
    public synchronized void receiveGraphInitEvent(GraphInitializedEvent<T> e) {
        super.receiveGraphInitEvent(e);
    }
}

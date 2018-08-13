package jaicore.graphvisualizer.gui;

import com.google.common.eventbus.Subscribe;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import org.graphstream.graph.Node;

import java.util.Random;


public class HeatVisualization<T> extends GraphVisualization<T> {

    private Random random;

    public HeatVisualization() {
        super();
        this.graph.clear();
        random = new Random();
    }

    @Override
    protected synchronized Node newNode(final T newNodeExt) {
        Node node = super.newNode(newNodeExt);
        System.out.println("test");
//        node.setAttribute("ui.style", "fill-color : blue;");
        node.setAttribute("ui.style", "fill-color: #"+Integer.toHexString(random.nextInt(256*256*256))+";");
        return node;

    }

    @Override
    public synchronized void receiveGraphInitEvent(GraphInitializedEvent<T> e) {
        super.receiveGraphInitEvent(e);
    }
}

package jaicore.graphvisualizer.gui;

import com.google.common.eventbus.Subscribe;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import org.graphstream.graph.Node;

import java.util.Random;


public class HeatVisualization<T> extends GraphVisualization<T> {

    private Random random;

    private double bestFValue;
    private double worstFValue;

    public HeatVisualization() {
        super();
        this.graph.clear();
//        this.graph.setAttribute("ui.stylesheet", "url('conf/searchgraph.css')");
        this.graph.setAttribute("ui.stylesheet", "url('conf/heatmap.css')");
        random = new Random();
        bestFValue = Double.MIN_VALUE;
        worstFValue = Double.MAX_VALUE;
    }

    @Override
    protected synchronized Node newNode(final T newNodeExt) {
        Node node = super.newNode(newNodeExt);
//        System.out.println("test");
//        node.setAttribute("ui.style", "fill-color: #"+Integer.toHexString(random.nextInt(256*256*256))+";");
        HeatValueSupplier s = (HeatValueSupplier) this.int2extNodeMap.get(node);
        if(s.getInternalLabel() instanceof Number){
            double fvalue =  ((Number) s.getInternalLabel()).doubleValue();

            if(fvalue > bestFValue)
                bestFValue = fvalue;
            if(fvalue < worstFValue)
                worstFValue = fvalue;
        }

        System.out.println("best: " + bestFValue + " worst: " + worstFValue);
        return node;

    }
}

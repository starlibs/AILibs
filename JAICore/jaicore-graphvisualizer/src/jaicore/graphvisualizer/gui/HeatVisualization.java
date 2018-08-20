package jaicore.graphvisualizer.gui;

import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class HeatVisualization<T> extends GraphVisualization<T> {

    private Random random;

    private double bestFValue;
    private double worstFValue;
    private List<Node> nodes;

    public HeatVisualization() {
        super();
        this.graph.clear();
//        this.graph.setAttribute("ui.stylesheet", "url('conf/searchgraph.css')");
        this.graph.setAttribute("ui.stylesheet", "url('conf/heatmap.css')");
        random = new Random();
        bestFValue = Double.MIN_VALUE;
        worstFValue = Double.MAX_VALUE;
        this.nodes = new ArrayList<>();
    }

    @Override
    protected synchronized Node newNode(final T newNodeExt) {
        Node node = super.newNode(newNodeExt);
//        System.out.println("test");
//        node.setAttribute("ui.style", "fill-color: #"+Integer.toHexString(random.nextInt(256*256*256))+";");
        HeatValueSupplier s = (HeatValueSupplier) this.int2extNodeMap.get(node);
        if(s.getInternalLabel() instanceof Number){
            double fvalue =  ((Number) s.getInternalLabel()).doubleValue();

            if(fvalue > bestFValue) {
                bestFValue = fvalue;
                update();
            }
            if(fvalue < worstFValue) {
                worstFValue = fvalue;
               update();
            }
            if(!roots.contains(newNodeExt)){
                colorNode(node, fvalue);
                nodes.add(node);
            }
        }

        System.out.println("best: " + bestFValue + " worst: " + worstFValue);
        return node;

    }

    public void update(){
        for(Node n :nodes){
            HeatValueSupplier s = (HeatValueSupplier) this.int2extNodeMap.get(n);
            if(s.getInternalLabel() instanceof Number) {
                double fvalue = ((Number) s.getInternalLabel()).doubleValue();
                colorNode(n, fvalue);
            }
        }

    }

    private void colorNode(Node node, double fvalue) {
        float color = random.nextFloat();
        float x = (float)(fvalue - worstFValue);
        float y = (float)(bestFValue - worstFValue);
        color = x/y;

        node.setAttribute("ui.color", color);
    }

}

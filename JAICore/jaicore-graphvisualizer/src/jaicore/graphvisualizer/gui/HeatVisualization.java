package jaicore.graphvisualizer.gui;

import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class HeatVisualization<T> extends GraphVisualization<T> {


    protected double bestFValue;
    protected double worstFValue;
    protected List<Node> nodes;

    public HeatVisualization() {
        super();
        this.graph.clear();
//        this.graph.setAttribute("ui.stylesheet", "url('conf/searchgraph.css')");
        this.graph.setAttribute("ui.stylesheet", "url('conf/heatmap.css')");
        bestFValue = Double.MAX_VALUE;
        worstFValue = Double.MIN_VALUE;
        this.nodes = new ArrayList<>();
    }

    @Override
    protected synchronized Node newNode(final T newNodeExt) {
        try {
            Node node = super.newNode(newNodeExt);
//        System.out.println("test");
//        node.setAttribute("ui.style", "fill-color: #"+Integer.toHexString(random.nextInt(256*256*256))+";");
            HeatValueSupplier s = (HeatValueSupplier) this.int2extNodeMap.get(node);
            if (s.getInternalLabel() instanceof Number) {
                double fvalue = ((Number) s.getInternalLabel()).doubleValue();

                if (fvalue < bestFValue) {
                    bestFValue = fvalue;
                    update();
                }
                if (fvalue > worstFValue) {
                    worstFValue = fvalue;
                    update();
                }

                if (!roots.contains(newNodeExt)) {
                    colorNode(node, fvalue);
                    nodes.add(node);
                }

            }
            System.out.println("best: " + bestFValue + " worst: " + worstFValue);
            return node;
        }
        catch(Exception e){
            e.printStackTrace();
            System.exit(0);
            return null;
        }

    }

    protected void update(){
        System.out.println("update");
        for(Node n :nodes){
            HeatValueSupplier s = (HeatValueSupplier) this.int2extNodeMap.get(n);
            if(s.getInternalLabel() instanceof Number) {
                double fvalue = ((Number) s.getInternalLabel()).doubleValue();
                colorNode(n, fvalue);
            }
        }

    }

    protected void colorNode(Node node, double fvalue) {
        float color = 1;
        float x = (float)(fvalue - bestFValue);
        float y = (float)(worstFValue - bestFValue);
        color = x/y;
        System.out.println("current " + fvalue + " color: " + color);
        node.setAttribute("ui.color", color);
    }


    @Override
    public void reset() {
        super.reset();
        this.graph.clear();
        this.graph.setAttribute("ui.stylesheet", "url('conf/heatmap.css')");
        this.nodes.clear();
    }

}

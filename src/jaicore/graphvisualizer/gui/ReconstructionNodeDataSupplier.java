package jaicore.graphvisualizer.gui;

import jaicore.graphvisualizer.INodeDataSupplier;
import jaicore.graphvisualizer.INodeDataVisualizer;

import java.util.HashMap;

public class ReconstructionNodeDataSupplier implements INodeDataSupplier {

    HashMap<Integer, String> data;
    INodeDataVisualizer visualizer;


    public ReconstructionNodeDataSupplier(HashMap map){
        this.data = new HashMap<Integer, String>();
        map.forEach((n,x)->{
            Integer i = Integer.parseInt((String) n);
            String s = (String) x;
            data.put(i,s);
        });
        this.visualizer = new ReconstructionNodeDataVisualizer();
    }

    @Override
    public void receiveEvent(Object event) {
        return;
    }

    @Override
    public INodeDataVisualizer getVisualization() {
        return this.visualizer;
    }

    @Override
    public void update(Object node) {
        String dataString = data.get(node);
        this.visualizer.update(dataString);
    }
}

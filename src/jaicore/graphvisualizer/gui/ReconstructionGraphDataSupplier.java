package jaicore.graphvisualizer.gui;

import jaicore.graphvisualizer.IGraphDataSupplier;
import jaicore.graphvisualizer.IGraphDataVisualizer;

import java.util.HashMap;

public class ReconstructionGraphDataSupplier implements IGraphDataSupplier {

    HashMap<Integer, String> data;
    IGraphDataVisualizer visualizer;


    public ReconstructionGraphDataSupplier(HashMap map){
        this.data = new HashMap<Integer, String>();
        map.forEach((n,x)->{
            Integer i = Integer.parseInt((String) n);
            String s = (String) x;
            data.put(i,s);
        });
        this.visualizer = new ReconstructionGraphDataVisualizer();
    }

    @Override
    public void receiveEvent(Object event) {
        return;
    }

    @Override
    public IGraphDataVisualizer getVisualization() {
        return this.visualizer;
    }

    @Override
    public void update(Object node) {
        String dataString = data.get(node);
        this.visualizer.update(dataString);
    }
}

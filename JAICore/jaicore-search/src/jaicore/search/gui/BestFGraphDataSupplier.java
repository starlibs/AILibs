package jaicore.search.gui;

import java.util.TreeMap;

import jaicore.graphvisualizer.IDataVisualizer;
import jaicore.graphvisualizer.IGraphDataSupplier;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeTypeSwitchEvent;
import jaicore.search.structure.core.Node;

public class BestFGraphDataSupplier implements IGraphDataSupplier {

    //TODO

    TreeMap<Long, Comparable>  map;

    BestFGraphDataVisualizer visu;

    public BestFGraphDataSupplier(){
        map = new TreeMap();
        visu = new BestFGraphDataVisualizer();
    }


    @Override
    public void update(long time, Object event){
        Node n;
        Comparable f;
        switch(event.getClass().getSimpleName()){

            case "GraphInitializedEvent":
                GraphInitializedEvent initializedEvent = (GraphInitializedEvent) event;
                n = (Node) initializedEvent.getRoot();
                map.put(time, n.getInternalLabel());
                break;

            case "NodeTypeSwitchEvent":
                NodeTypeSwitchEvent nodeTypeSwitchEvent = (NodeTypeSwitchEvent) event;
                n = (Node) nodeTypeSwitchEvent.getNode();
                f= map.get(map.lastKey());
                if(n.getInternalLabel().compareTo(f) <= 0)
                    map.put(time, n.getInternalLabel());
                else
                    map.put(time, f);

                break;

            case "NodeReachedEvent":
                NodeReachedEvent nodeReachedEvent = (NodeReachedEvent) event;
                n = (Node) nodeReachedEvent.getNode();
                f= map.get(map.lastKey());
                if(n.getInternalLabel().compareTo(f) <= 0)
                    map.put(time, n.getInternalLabel());
                else
                    map.put(time, f);
                break;

            default:
                System.out.println("not an allowed event");
                break;
        }
        this.visu.update(map.lastKey(), (double)map.get(map.lastKey()));
    }

    @Override
    public IDataVisualizer getVisualization() {
        return visu;
    }

    @Override
    public void receiveEvent(Object event) {

    }
}

package jaicore.graphvisualizer;

import jaicore.graphvisualizer.events.GraphInitializedEvent;
import jaicore.graphvisualizer.events.NodeReachedEvent;
import jaicore.graphvisualizer.events.NodeTypeSwitchEvent;
import jaicore.search.structure.core.Node;

import java.util.TreeMap;

public class BestFGraphDataSupplier implements IGraphDataSupplier {

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

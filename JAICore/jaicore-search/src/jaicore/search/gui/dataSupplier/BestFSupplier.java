package jaicore.search.gui.dataSupplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.events.controlEvents.ControlEvent;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeTypeSwitchEvent;
import jaicore.graphvisualizer.events.graphEvents.GraphEvent;
import jaicore.graphvisualizer.events.misc.XYEvent;
import jaicore.graphvisualizer.gui.dataSupplier.ISupplier;
import jaicore.search.structure.core.Node;
import javafx.scene.chart.XYChart;

import java.util.ArrayList;
import java.util.List;

public class BestFSupplier implements ISupplier {

    private EventBus databus;

    private List<Comparable> list;
    private int index;


    public BestFSupplier(){
        this.list = new ArrayList<>();
        this.databus = new EventBus();
        this.index = 0;
    }


    @Override
    public void registerListener(Object listener) {
        this.databus.register(listener);
    }

//    @Override
//    public String getVisualizerName() {
//        return "XYGraphVisualizer";
//    }
//
//    @Override
//    public String getVisualizerTitle() {
//        return "BestF";
//    }

    @Subscribe
    public void receiveGraphEvent(GraphEvent event) {
        Node n;
        Comparable f;
        switch(event.getClass().getSimpleName()){

            case "GraphInitializedEvent":
                GraphInitializedEvent initializedEvent = (GraphInitializedEvent) event;
                n = (Node) initializedEvent.getRoot();
                list.add(n.getInternalLabel());
                break;

            case "NodeTypeSwitchEvent":
                NodeTypeSwitchEvent nodeTypeSwitchEvent = (NodeTypeSwitchEvent) event;
                n = (Node) nodeTypeSwitchEvent.getNode();
                f= list.get(list.size()-1);
                if(n.getInternalLabel().compareTo(f) <= 0)
                    list.add( n.getInternalLabel());
                else
                    list.add(f);

                break;

            case "NodeReachedEvent":
                NodeReachedEvent nodeReachedEvent = (NodeReachedEvent) event;
                n = (Node) nodeReachedEvent.getNode();
                f= list.get(list.size()-1);
                if(n.getInternalLabel().compareTo(f) <= 0)
                    list.add(n.getInternalLabel());
                else
                    list.add(f);
                break;

            default:
                System.out.println("not an allowed event");
                break;
        }

        XYChart.Data data = new XYChart.Data(index, (double)list.get(index));
        this.databus.post(new XYEvent(data));
        index ++;

    }

    @Override
    public void receiveControlEvent(ControlEvent event) {

    }

    @Override
    public JsonNode getSerialization() {
        return null;
    }


//    @Subscribe
//    public void receiveStepEvent(StepEvent event){
//        if(!event.forward())
//            return;
//
//        int steps = event.getSteps();
//        while(steps != 0 ){
//            XYChart.Data data = new XYChart.Data(index, (double)list.get(index));
//            this.databus.post(new XYEvent(data));
//           steps --;
//           index ++;
//        }
//
//    }


}

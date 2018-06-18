package jaicore.graphvisualizer.gui.dataSupplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import jaicore.graphvisualizer.events.VisuEvent;
import jaicore.graphvisualizer.events.add.HTMLEvent;
import jaicore.graphvisualizer.events.controlEvents.ControlEvent;
import jaicore.graphvisualizer.events.controlEvents.NodePushed;


import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ReconstructionDataSupplier implements ISupplier {

    private LinkedHashMap map;
    private String title;
    private String visualizer;

    private EventBus dataBus;

    private ArrayList doEvents;
    private LinkedHashMap nodeData;


    public ReconstructionDataSupplier(LinkedHashMap map){
        this.map = map;
        this.title = (String) map.get("Title");
        this.visualizer = (String) map.get("Visualizer");
        this.dataBus = new EventBus();
        this.nodeData = new LinkedHashMap();
        this.nodeData = (LinkedHashMap) map.get("Data");
        doEvents = (ArrayList) map.get("DoEvents");
    }

    @Override
    public void registerListener(Object listener) {
        dataBus.register(listener);
    }

//    @Override
//    public String getVisualizerName() {
//        return visualizer;
//    }
//
//    @Override
//    public String getVisualizerTitle() {
//        return title;
//    }

    @Override
    public void receiveGraphEvent(VisuEvent event) {

    }

    @Subscribe
    public void receiveControlEvent(ControlEvent event) {
        String eventName = event.getClass().getSimpleName();
        switch (eventName){
            case "NodePushed":
                NodePushed nodePushed = (NodePushed) event;
                if(visualizer.equals("HTMLVisualizer")) {
                    Integer hashCode = nodePushed.getNode().hashCode();
                    System.out.println(hashCode);
                    String dataString = (String) nodeData.get(hashCode.toString());
                    this.dataBus.post(new HTMLEvent(dataString));
                }

                break;
        }
    }

    @Override
    public JsonNode getSerialization() {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.valueToTree(map);
    }



    private Object findClassByName(String name){
        String packageName = "jaicore.graphvisualizer.gui.events.controlEvents";

        try{
            Class<?> cls = Class.forName(packageName+"."+ name);
            return cls.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}

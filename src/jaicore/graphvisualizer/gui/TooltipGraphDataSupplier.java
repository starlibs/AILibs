package jaicore.graphvisualizer.gui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jaicore.graphvisualizer.IGraphDataSupplier;
import jaicore.graphvisualizer.TooltipGenerator;
import jaicore.graphvisualizer.events.GraphInitializedEvent;
import jaicore.graphvisualizer.events.NodeReachedEvent;
import jaicore.search.structure.core.Node;

import java.util.HashMap;
import java.util.Map;

public class TooltipGraphDataSupplier implements IGraphDataSupplier {

    private TooltipGenerator tooltipGenerator;

    private Map<Integer, String> tooltipMap;

    TooltipVisualizer visu;

    public TooltipGraphDataSupplier (){
        this.tooltipMap = new HashMap();
        this.tooltipGenerator = null;
        visu = new TooltipVisualizer();
    }

    @Override
    public void receiveEvent(Object event) {
        switch(event.getClass().getSimpleName()){
            case "GraphInitializedEvent":
                GraphInitializedEvent initializedEvent = (GraphInitializedEvent) event;
                String tooltip = tooltipGenerator.getTooltip((Node)initializedEvent.getRoot());
                tooltipMap.put(initializedEvent.getRoot().hashCode(), tooltip);
                break;

            case "NodeTypeSwitchEvent":
                break;

            case "NodeReachedEvent":
                NodeReachedEvent nodeReachedEvent = (NodeReachedEvent) event;
                tooltipMap.put(nodeReachedEvent.getNode().hashCode(), tooltipGenerator.getTooltip(nodeReachedEvent.getNode()));
                break;

            default:
                break;
        }
    }


    @Override
    public JsonNode getSerialization() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            System.out.println(mapper.writeValueAsString(tooltipMap));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setTooltipGenerator(TooltipGenerator gen){
        this.tooltipGenerator = gen;
    }


    public String getData(Node node){
        return tooltipMap.get(node.hashCode());
    }

    public javafx.scene.Node getVisualization(){
        return visu.getVisualization();
    }

    public void update(Object node){
        visu.update(getData((Node)node));
    }
}

package jaicore.graphvisualizer.gui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jaicore.graphvisualizer.INodeDataSupplier;
import jaicore.graphvisualizer.INodeDataVisualizer;
import jaicore.graphvisualizer.TooltipGenerator;
import jaicore.graphvisualizer.events.GraphInitializedEvent;
import jaicore.graphvisualizer.events.NodeReachedEvent;
import jaicore.graphvisualizer.events.NodeTypeSwitchEvent;

import java.util.HashMap;
import java.util.Map;

public class TooltipGraphDataSupplier implements INodeDataSupplier {

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
                String tooltip = tooltipGenerator.getTooltip(initializedEvent.getRoot());
                tooltipMap.put(initializedEvent.getRoot().hashCode(), tooltip);
                break;

            case "NodeTypeSwitchEvent":
                NodeTypeSwitchEvent switchEvent = (NodeTypeSwitchEvent) event;
                if(tooltipMap.containsKey(switchEvent.getNode().hashCode()))
                    tooltipMap.put(switchEvent.getNode().hashCode(), tooltipGenerator.getTooltip(switchEvent.getNode()));
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
        JsonNode node=  mapper.valueToTree(tooltipMap);
        return node;
    }

    public void setTooltipGenerator(TooltipGenerator gen){
        this.tooltipGenerator = gen;
    }


    public String getData(Object node){
        return tooltipMap.get(node.hashCode());
    }

    public INodeDataVisualizer getVisualization(){
        return visu;    }

    public void update(Object node){
        visu.update(getData(node));
    }

}

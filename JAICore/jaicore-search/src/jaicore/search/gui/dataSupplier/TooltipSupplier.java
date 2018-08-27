package jaicore.search.gui.dataSupplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import jaicore.graphvisualizer.TooltipGenerator;
import jaicore.graphvisualizer.events.controlEvents.ControlEvent;
import jaicore.graphvisualizer.events.controlEvents.NodePushed;
import jaicore.graphvisualizer.events.graphEvents.GraphEvent;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeTypeSwitchEvent;
import jaicore.graphvisualizer.events.misc.HTMLEvent;
import jaicore.graphvisualizer.gui.dataSupplier.ISupplier;
import jaicore.search.structure.core.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Supplier which contains tooltips for the nodes and post them so that a tooltip supplier can show them.
 * @author jkoepe
 *
 */
public class TooltipSupplier implements ISupplier {

    private EventBus dataBus;
    private TooltipGenerator generator;
    private String title;

    private Map<Integer, String> tooltipMap;

    public TooltipSupplier() {
        this.dataBus = new EventBus();
        this.tooltipMap = new HashMap<>();
        this.title = "Tooltips";
    }


    public void setGenerator(TooltipGenerator generator) {
        this.generator = generator;
    }

    @Override
    public JsonNode getSerialization() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        HashMap<String, Object> map = new HashMap();

//        map.put("Visualizer", visualizer);
        map.put("Title", title);
        map.put("Data", tooltipMap);

        List<String> doEvents = new ArrayList<>();
        doEvents.add("NodePushed");
        map.put("DoEvents", doEvents);

        return mapper.valueToTree(map);
    }


    @Subscribe
    public void receiveControlEvent(ControlEvent event){
        if(event instanceof NodePushed)
            this.dataBus.post(new HTMLEvent(tooltipMap.get(((NodePushed)event).getNode().hashCode())));
    }

    @Override
    public void registerListener(Object listener) {
        this.dataBus.register(listener);
    }

    @Subscribe
    public void receiveGraphEvent(GraphEvent event) {
        switch(event.getClass().getSimpleName()){
            case "GraphInitializedEvent":
                GraphInitializedEvent initializedEvent = (GraphInitializedEvent) event;
                String tooltip = generator.getTooltip((Node)initializedEvent.getRoot());
                tooltipMap.put(initializedEvent.getRoot().hashCode(), tooltip);
                break;

            case "NodeTypeSwitchEvent":
                NodeTypeSwitchEvent switchEvent = (NodeTypeSwitchEvent) event;
                if(tooltipMap.containsKey(switchEvent.getNode().hashCode()))
                    tooltipMap.put(switchEvent.getNode().hashCode(), generator.getTooltip(switchEvent.getNode()));
                break;

            case "NodeReachedEvent":
                NodeReachedEvent nodeReachedEvent = (NodeReachedEvent) event;
                tooltipMap.put(nodeReachedEvent.getNode().hashCode(), generator.getTooltip(nodeReachedEvent.getNode()));
                break;

            default:
                break;
        }
    }


}

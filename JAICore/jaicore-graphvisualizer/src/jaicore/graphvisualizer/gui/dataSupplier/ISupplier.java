package jaicore.graphvisualizer.gui.dataSupplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.eventbus.EventBus;
import jaicore.graphvisualizer.events.VisuEvent;
import jaicore.graphvisualizer.events.controlEvents.ControlEvent;

public interface ISupplier {


    void registerListener(Object listener);


    void receiveGraphEvent(VisuEvent event);
    void receiveControlEvent(ControlEvent event);

    JsonNode getSerialization();



}

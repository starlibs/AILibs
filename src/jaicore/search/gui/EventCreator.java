package jaicore.search.gui;

import jaicore.search.structure.core.Node;
import jaicore.search.structure.events.GraphInitializedEvent;

import java.util.LinkedHashMap;
import java.util.Set;


public class EventCreator {

    public Object createEvent(LinkedHashMap jMap){
        if(jMap.keySet().contains("root")){
            LinkedHashMap map = (LinkedHashMap)jMap.get("root");
            GuiNode guiNode = new GuiNode((LinkedHashMap)map.get("point"));

            Node node = new Node(null, guiNode);
            node.setGoal(false);
            node.setInternalLabel((double) 0.0);

            return new GraphInitializedEvent<>(node);

        }
        return null;

    }

}

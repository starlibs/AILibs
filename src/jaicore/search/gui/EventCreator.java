package jaicore.search.gui;

import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.events.GraphInitializedEvent;
import jaicore.search.structure.events.NodeReachedEvent;
import jaicore.search.structure.events.NodeTypeSwitchEvent;
import sun.awt.image.ImageWatched;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;


public class EventCreator {

    //the unique indicator for the Map is right now just working for queennodes
    HashMap nodeMap;

    public EventCreator(){
        nodeMap = new HashMap<>();
    }


    //Create a new GraphIinitializedEvent
    public Object createEvent(LinkedHashMap jMap){


        if(jMap.get("name").equals("GraphInitializedEvent")){

            LinkedHashMap map = (LinkedHashMap)jMap.get("root");

            GuiNode guiNode = new GuiNode((LinkedHashMap)map.get("point"));
            int id = computeId((LinkedHashMap)map.get("point"));
            guiNode.setId(id);

            Node node = new Node(null, guiNode);
            node.setGoal(false);
            node.setInternalLabel((double) 0.0);

            nodeMap.put(0, node);

            return new GraphInitializedEvent<>(node);

        }



        if(jMap.get("name").equals("NodeParentSwitchEvent"))
            System.out.println("NodeParentSwitchEvent");

        if(jMap.get("name").equals("NodeReachedEvent")){

            LinkedHashMap parentMap = (LinkedHashMap)jMap.get("parent");
            int parentId = computeId((LinkedHashMap) parentMap.get("point"));
            Node parent = (Node) nodeMap.get(parentId);


            LinkedHashMap map = (LinkedHashMap)jMap.get("node");
            int nodeId = computeId((LinkedHashMap)map.get("poin"));
            System.out.println(map.keySet());

            return null;//new NodeReachedEvent<>(parent, node, type);
        }

        if(jMap.get("name").equals("NodeRemovedEvent"))
            System.out.println("NodeRemovedEvent");

        if(jMap.get("name").equals("NodeTypeSwitchEvent")){
            LinkedHashMap map = (LinkedHashMap)jMap.get("node");
            int id = computeId((LinkedHashMap)map.get("point"));
            String type = (String) jMap.get("type");
            return new NodeTypeSwitchEvent<>(nodeMap.get(id), type);
        }

        return null;

    }



    private int computeId(LinkedHashMap map){
        ArrayList list = (ArrayList) map.get("positions");
        if(list.isEmpty())
            return 0;
        int result = 0;
        return -1;
    }

}

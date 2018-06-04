package jaicore.graphvisualizer.gui;

import jaicore.search.structure.core.Node;
import jaicore.graphvisualizer.events.GraphInitializedEvent;
import jaicore.graphvisualizer.events.NodeReachedEvent;
import jaicore.graphvisualizer.events.NodeTypeSwitchEvent;

import java.util.HashMap;
import java.util.LinkedHashMap;


public class EventCreator {

    //the unique indicator for the Map is right now just working for queennodes
    HashMap nodeMap;

    public EventCreator(){
        nodeMap = new HashMap<>();
    }


    //Create a new GraphIinitializedEvent
    public Object createEvent(LinkedHashMap jMap){

        //create new GraphInitializedEveent
        if(jMap.get("name").equals("GraphInitializedEvent")){

            LinkedHashMap map = (LinkedHashMap)jMap.get("root");

            //extract Node
            GuiNode guiNode = new GuiNode((LinkedHashMap)map.get("point"));
            int id = computeId((LinkedHashMap)map.get("point"));
            guiNode.setId(id);

            //create new Node based on extracted one
            Node node = new Node(null, guiNode);
            node.setGoal(false);
            node.setInternalLabel((double) 0.0);

            nodeMap.put(id, node);

            return new GraphInitializedEvent<>(node);

        }

        //create a new NodeParentSwithcEvent
        if(jMap.get("name").equals("NodeParentSwitchEvent"))
            System.out.println("NodeParentSwitchEvent");

        // create a new NodeReachedEvent
        if(jMap.get("name").equals("NodeReachedEvent")){

            //extract parent node
            LinkedHashMap parentMap = (LinkedHashMap)jMap.get("parent");
            int parentId = computeId((LinkedHashMap) parentMap.get("point"));
            Object parent = nodeMap.get(parentId);

            //extract current node
            LinkedHashMap map = (LinkedHashMap)jMap.get("node");
            int nodeId = computeId((LinkedHashMap)map.get("point"));
            GuiNode guiNode = new GuiNode((LinkedHashMap) map.get("point"));
            guiNode.setId(nodeId);

            //create a new node with the extracted node
            Node node = new Node(null, guiNode);
            node.setGoal(false);
            node.setInternalLabel((double) 0.0);

            nodeMap.put(nodeId, node);

            String type = (String) jMap.get("type");

            return new NodeReachedEvent<>(parent, node, type);
        }

        //create a new NodeRemovedEvent
        if(jMap.get("name").equals("NodeRemovedEvent"))
            System.out.println("NodeRemovedEvent");

        //create a new NodeTypeSwitchEvent
        if(jMap.get("name").equals("NodeTypeSwitchEvent")){
            LinkedHashMap map = (LinkedHashMap)jMap.get("node");
            int id = computeId((LinkedHashMap)map.get("point"));
            String type = (String) jMap.get("type");
            return new NodeTypeSwitchEvent<>(nodeMap.get(id), type);
        }

        return null;

    }



    private int computeId(LinkedHashMap map){
        if(map.keySet().contains("id"))
            return (int) map.get("id");
        else
            return -1;
    }
}

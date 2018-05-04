package jaicore.graphvisualizer.gui;

import java.util.LinkedHashMap;


/**
 * This class is a simple node class, which is used to recreate events from a JASO-String
 */
public class GuiNode {


    private LinkedHashMap map;



    private int id;

    public GuiNode(LinkedHashMap map){
        this.map = map;
    }


    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

package jaicore.search.gui;

import java.util.LinkedHashMap;

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

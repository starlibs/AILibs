package jaicore.graphvisualizer.gui;

import jaicore.graphvisualizer.TooltipGenerator;

import java.io.Serializable;

public class TooltipTest implements TooltipGenerator{


    @Override
    public String getTooltip(Object node) {
       return node.toString();
    }
}

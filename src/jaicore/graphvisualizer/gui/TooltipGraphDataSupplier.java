package jaicore.graphvisualizer.gui;

import com.fasterxml.jackson.databind.JsonNode;
import jaicore.graphvisualizer.IGraphDataSupplier;
import jaicore.graphvisualizer.TooltipGenerator;

public class TooltipGraphDataSupplier implements IGraphDataSupplier {

    private TooltipGenerator tooltipGenerator;

    @Override
    public void receiveEvent(Object event) {

    }


    @Override
    public JsonNode getSerialization() {
        return null;
    }

    public void setTooltipGenerator(TooltipGenerator gen){
        this.tooltipGenerator = gen;
    }
}

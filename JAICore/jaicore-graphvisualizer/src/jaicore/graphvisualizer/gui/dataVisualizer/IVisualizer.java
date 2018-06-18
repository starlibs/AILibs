package jaicore.graphvisualizer.gui.dataVisualizer;

import javafx.scene.Node;

public interface IVisualizer {

    public Node getVisualization();

    default String getSupplier() {
        return "";
    }

    default String getTitle(){
        return "";
    }
}

package jaicore.graphvisualizer;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.Node;

import java.util.HashMap;

public interface IGraphDataSupplier<T> {

    void receiveEvent(T event);


    default JsonNode getSerialization() {
        return null;
    }


    IGraphDataVisualizer getVisualization();

    void update(Object node);



}

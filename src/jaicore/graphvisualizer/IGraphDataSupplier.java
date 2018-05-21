package jaicore.graphvisualizer;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.Node;

public interface IGraphDataSupplier<T> {

    void receiveEvent(T event);


    default JsonNode getSerialization() {
        return null;
    }


    Node getVisualization();

    public void update(Object node);




}

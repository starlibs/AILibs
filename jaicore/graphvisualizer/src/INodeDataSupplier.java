package jaicore.graphvisualizer;

import com.fasterxml.jackson.databind.JsonNode;

public interface INodeDataSupplier<T> {

    void receiveEvent(T event);


    default JsonNode getSerialization() {
        return null;
    }


    INodeDataVisualizer getVisualization();

    void update(Object node);



}

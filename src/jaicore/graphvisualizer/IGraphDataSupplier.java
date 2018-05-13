package jaicore.graphvisualizer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

public interface IGraphDataSupplier<T> {

    void receiveEvent(T event);


    default JsonNode getSerialization() {
        return null;
    }


}

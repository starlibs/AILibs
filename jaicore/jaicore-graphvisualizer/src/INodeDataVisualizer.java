package jaicore.graphvisualizer;

import javafx.scene.Node;

public interface INodeDataVisualizer extends IDataVisualizer {

    public Node getVisualization();

    public void update( String data);
}

package jaicore.graphvisualizer;

import javafx.scene.Node;

public interface IGraphDataVisualizer {

    public Node getVisualization();

    public void update( String data);
}

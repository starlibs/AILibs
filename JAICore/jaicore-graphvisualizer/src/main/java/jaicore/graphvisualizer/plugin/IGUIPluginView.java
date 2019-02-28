package jaicore.graphvisualizer.plugin;

import javafx.scene.Node;

public interface IGUIPluginView {

	public Node getNode();

	public void update();

	public String getTitle();
}

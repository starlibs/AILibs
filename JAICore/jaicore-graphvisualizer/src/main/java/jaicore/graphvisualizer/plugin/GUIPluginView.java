package jaicore.graphvisualizer.plugin;

import javafx.scene.Node;

public interface GUIPluginView {

	public Node getNode();

	public void update();

	public String getTitle();
}

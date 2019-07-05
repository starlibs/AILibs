package ai.libs.jaicore.graphvisualizer.plugin;

import ai.libs.jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import javafx.scene.Node;

/**
 * An {@link IGUIPluginView} is part of an {@link IGUIPlugin} and is responsible displaying the information stored in the {@link IGUIPluginModel}.
 * 
 * @author atornede
 *
 */
public interface IGUIPluginView {

	/**
	 * Returns the JavaFX Scene {@link Node} which will be displayed inside the {@link AlgorithmVisualizationWindow}.
	 * 
	 * @return The JavaFX Scene {@link Node} which will be displayed inside the {@link AlgorithmVisualizationWindow}
	 */
	public Node getNode();

	/**
	 * Requests this view to update itself, i.e. pull the latest information from the associated {@link IGUIPluginModel} and display it.
	 */
	public void update();

	/**
	 * Returns the title of the view of the associated {@link IGUIPlugin}.
	 * 
	 * @return The title of the view of the associated {@link IGUIPlugin}.
	 */
	public String getTitle();
}

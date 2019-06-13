package ai.libs.jaicore.graphvisualizer.plugin;

import ai.libs.jaicore.graphvisualizer.events.graph.bus.AlgorithmEventSource;
import ai.libs.jaicore.graphvisualizer.events.gui.GUIEventSource;

public interface IGUIPlugin {

	public IGUIPluginController getController();

	public IGUIPluginModel getModel();

	public IGUIPluginView getView();


	public void setAlgorithmEventSource(AlgorithmEventSource graphEventSource);

	public void setGUIEventSource(GUIEventSource guiEventSource);

}

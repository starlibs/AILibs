package jaicore.graphvisualizer.plugin;

import jaicore.graphvisualizer.events.graph.bus.AlgorithmEventSource;
import jaicore.graphvisualizer.events.gui.GUIEventSource;

public interface IGUIPlugin {

	public IGUIPluginController getController();

	public IGUIPluginModel getModel();

	public IGUIPluginView getView();


	public void setAlgorithmEventSource(AlgorithmEventSource graphEventSource);

	public void setGUIEventSource(GUIEventSource guiEventSource);

}

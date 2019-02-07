package jaicore.graphvisualizer.plugin;

import jaicore.graphvisualizer.events.graph.bus.GraphEventSource;
import jaicore.graphvisualizer.events.gui.GUIEventSource;

public interface GUIPlugin {

	public GUIPluginController getController();

	public GUIPluginModel getModel();

	public GUIPluginView getView();

	public void setGraphEventSource(GraphEventSource graphEventSource);

	public void setGUIEventSource(GUIEventSource guiEventSource);

}

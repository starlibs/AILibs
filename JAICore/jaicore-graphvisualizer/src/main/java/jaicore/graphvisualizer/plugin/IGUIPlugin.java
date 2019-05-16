package jaicore.graphvisualizer.plugin;

import jaicore.graphvisualizer.events.gui.GUIEventSource;
import jaicore.graphvisualizer.events.recorder.property.PropertyProcessedAlgorithmEventSource;

public interface IGUIPlugin {

	public IGUIPluginController getController();

	public IGUIPluginModel getModel();

	public IGUIPluginView getView();

	public void setAlgorithmEventSource(PropertyProcessedAlgorithmEventSource algorithmEventSource);

	public void setGUIEventSource(GUIEventSource guiEventSource);

}

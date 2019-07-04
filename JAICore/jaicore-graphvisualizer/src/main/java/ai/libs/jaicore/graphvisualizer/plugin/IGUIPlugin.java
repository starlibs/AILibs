package ai.libs.jaicore.graphvisualizer.plugin;

import ai.libs.jaicore.graphvisualizer.events.gui.GUIEventSource;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.PropertyProcessedAlgorithmEventSource;

public interface IGUIPlugin {

	public IGUIPluginController getController();

	public IGUIPluginModel getModel();

	public IGUIPluginView getView();


	public void setAlgorithmEventSource(PropertyProcessedAlgorithmEventSource graphEventSource);

	public void setGUIEventSource(GUIEventSource guiEventSource);

}

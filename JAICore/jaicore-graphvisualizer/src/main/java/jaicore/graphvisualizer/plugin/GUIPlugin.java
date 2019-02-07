package jaicore.graphvisualizer.plugin;

import jaicore.graphvisualizer.events.graph.bus.AlgorithmEventSource;
import jaicore.graphvisualizer.events.gui.GUIEventSource;
import jaicore.graphvisualizer.events.recorder.AlgorithmEventHistory;

public interface GUIPlugin {

	public GUIPluginController getController();

	public GUIPluginModel getModel();

	public GUIPluginView getView();

	public void setAlgorithmEventSource(AlgorithmEventSource graphEventSource);

	public void setGUIEventSource(GUIEventSource guiEventSource);

	public void setHistory(AlgorithmEventHistory history);

}

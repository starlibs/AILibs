package jaicore.graphvisualizer.events.gui;

public interface GUIEventBus extends GUIEventSource {

	public void postEvent(GUIEvent guiEvent);

}

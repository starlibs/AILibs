package jaicore.graphvisualizer.events.gui;

public interface GUIEventSource {

	public void registerListener(GUIEventListener guiEventListener);

	public void unregisterListener(GUIEventListener guiEventListener);
}

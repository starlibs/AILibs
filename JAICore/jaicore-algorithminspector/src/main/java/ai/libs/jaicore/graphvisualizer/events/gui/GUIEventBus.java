package ai.libs.jaicore.graphvisualizer.events.gui;

import ai.libs.jaicore.graphvisualizer.events.recorder.AlgorithmEventHistoryEntryDeliverer;

public interface GUIEventBus extends GUIEventSource {

	public void registerAlgorithmEventHistoryEntryDeliverer(AlgorithmEventHistoryEntryDeliverer historyEntryDeliverer);

	public void postEvent(GUIEvent guiEvent);

}

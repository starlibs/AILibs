package jaicore.graphvisualizer.events.gui;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DefaultGUIEventBus implements GUIEventBus {

	private static DefaultGUIEventBus singletonInstance;

	private List<GUIEventListener> guiEventListeners;

	private DefaultGUIEventBus() {
		guiEventListeners = Collections.synchronizedList(new LinkedList<>());
	}

	@Override
	public void registerListener(GUIEventListener graphEventListener) {
		guiEventListeners.add(graphEventListener);
	}

	@Override
	public void unregisterListener(GUIEventListener graphEventListener) {
		guiEventListeners.remove(graphEventListener);
	}

	@Override
	public void postEvent(GUIEvent guiEvent) {
		synchronized (this) {
			for (GUIEventListener listener : guiEventListeners) {
				listener.handleGUIEvent(guiEvent);
			}
		}
	}

	public static synchronized GUIEventBus getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new DefaultGUIEventBus();
		}
		return singletonInstance;
	}

}

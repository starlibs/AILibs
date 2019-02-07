package jaicore.graphvisualizer.events.gui;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultGUIEventBus implements GUIEventBus {

	private static DefaultGUIEventBus singletonInstance;

	private Set<GUIEventListener> guiEventListeners;

	private DefaultGUIEventBus() {
		guiEventListeners = ConcurrentHashMap.newKeySet();
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

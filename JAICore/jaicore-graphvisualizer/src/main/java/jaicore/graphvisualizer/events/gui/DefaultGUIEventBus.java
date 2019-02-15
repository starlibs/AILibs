package jaicore.graphvisualizer.events.gui;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultGUIEventBus implements GUIEventBus {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGUIEventBus.class);

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
				passEventToListener(guiEvent, listener);
			}
		}
	}

	private void passEventToListener(GUIEvent guiEvent, GUIEventListener listener) {
		try {
			listener.handleGUIEvent(guiEvent);
		} catch (Exception exception) {
			LOGGER.error("Error while passing GUIEvent {} to handler {}.", guiEvent, listener, exception);
		}
	}

	public static synchronized GUIEventBus getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new DefaultGUIEventBus();
		}
		return singletonInstance;
	}

}

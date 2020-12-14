package ai.libs.jaicore.graphvisualizer.events.gui;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.graphvisualizer.events.recorder.AlgorithmEventHistoryEntryDeliverer;

public class DefaultGUIEventBus implements GUIEventBus {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGUIEventBus.class);

	private static DefaultGUIEventBus singletonInstance;

	private List<GUIEventListener> guiEventListeners;
	private AlgorithmEventHistoryEntryDeliverer historyEntryDeliverer;

	private DefaultGUIEventBus() {
		this.guiEventListeners = Collections.synchronizedList(new LinkedList<>());
	}

	@Override
	public void registerAlgorithmEventHistoryEntryDeliverer(final AlgorithmEventHistoryEntryDeliverer historyEntryDeliverer) {
		this.historyEntryDeliverer = historyEntryDeliverer;
	}

	@Override
	public void registerListener(final GUIEventListener graphEventListener) {
		this.guiEventListeners.add(graphEventListener);
	}

	@Override
	public void unregisterListener(final GUIEventListener graphEventListener) {
		this.guiEventListeners.remove(graphEventListener);
	}

	@Override
	public void postEvent(final GUIEvent guiEvent) {
		synchronized (this) {
			for (GUIEventListener listener : this.guiEventListeners) {
				this.passEventToListener(guiEvent, listener);
			}
			this.passEventToListener(guiEvent, this.historyEntryDeliverer);
		}
	}

	private void passEventToListener(final GUIEvent guiEvent, final GUIEventListener listener) {
		try {
			listener.handleGUIEvent(guiEvent);
		} catch (Exception exception) {
			LOGGER.error("Error while passing GUIEvent {} to handler {}.", guiEvent, listener, exception);
		}
	}

	public static synchronized GUIEventBus getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new DefaultGUIEventBus();
		}
		return singletonInstance;
	}

}

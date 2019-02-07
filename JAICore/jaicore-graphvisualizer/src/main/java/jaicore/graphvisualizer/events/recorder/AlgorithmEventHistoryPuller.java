package jaicore.graphvisualizer.events.recorder;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.graphvisualizer.events.graph.bus.AlgorithmEventListener;
import jaicore.graphvisualizer.events.graph.bus.AlgorithmEventSource;
import jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;
import jaicore.graphvisualizer.events.gui.GUIEvent;
import jaicore.graphvisualizer.events.gui.GUIEventListener;
import jaicore.graphvisualizer.plugin.controlbar.PauseEvent;
import jaicore.graphvisualizer.plugin.controlbar.PlayEvent;
import jaicore.graphvisualizer.plugin.controlbar.ResetEvent;

public class AlgorithmEventHistoryPuller extends Thread implements AlgorithmEventSource, GUIEventListener {

	private Set<AlgorithmEventListener> algorithmEventListeners;
	private AlgorithmEventHistory eventHistory;

	// TODO move time and paused into a model
	private int timestep;
	private boolean paused;
	private int sleepTimeInMillis;

	public AlgorithmEventHistoryPuller(AlgorithmEventHistory eventHistory, int sleepTimeInMillis) {
		this.eventHistory = eventHistory;
		this.sleepTimeInMillis = sleepTimeInMillis;
		this.timestep = 0;
		this.paused = false;
		this.algorithmEventListeners = ConcurrentHashMap.newKeySet();
	}

	@Override
	public void registerListener(AlgorithmEventListener algorithmEventListener) {
		algorithmEventListeners.add(algorithmEventListener);
	}

	@Override
	public void unregisterListener(AlgorithmEventListener algorithmEventListener) {
		algorithmEventListeners.remove(algorithmEventListener);
	}

	@Override
	public void run() {
		while (!paused) {
			if (timestep < eventHistory.getLength()) {
				AlgorithmEvent algorithmEvent = eventHistory.getEntryAtTimeStep(timestep).getAlgorithmEvent();
				for (AlgorithmEventListener eventListener : algorithmEventListeners) {
					try {
						eventListener.handleAlgorithmEvent(algorithmEvent);
					} catch (HandleAlgorithmEventException e) {
						// TODO LOG THIS ERROR
					}
				}
				timestep++;
			}
			try {
				sleep(sleepTimeInMillis);
			} catch (InterruptedException e) {
				// TODO handle this
			}
		}
	}

	@Override
	public void handleGUIEvent(GUIEvent guiEvent) {
		if (guiEvent instanceof PauseEvent) {
			pause();
		} else if (guiEvent instanceof PlayEvent) {
			unpause();
		} else if (guiEvent instanceof ResetEvent) {
			resetTimeStep();
		}
	}

	private void pause() {
		paused = true;
	}

	private void unpause() {
		paused = false;
	}

	private void resetTimeStep() {
		timestep = 0;
	}

}

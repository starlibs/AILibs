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
import jaicore.graphvisualizer.plugin.speedslider.ChangeSpeedEvent;
import jaicore.graphvisualizer.plugin.timeslider.GoToTimeStepEvent;

public class AlgorithmEventHistoryPuller extends Thread implements AlgorithmEventSource, GUIEventListener {

	private Set<AlgorithmEventListener> algorithmEventListeners;
	private AlgorithmEventHistory eventHistory;

	// TODO move time and paused into a model
	private int timestep;
	private boolean paused;
	private int maximumSleepTimeInMilliseconds;
	private double sleepTimeMultiplier;

	public AlgorithmEventHistoryPuller(AlgorithmEventHistory eventHistory, int maximumSleepTimeInMilliseconds) {
		this.eventHistory = eventHistory;
		this.maximumSleepTimeInMilliseconds = maximumSleepTimeInMilliseconds;

		this.timestep = 0;
		this.paused = true;
		this.algorithmEventListeners = ConcurrentHashMap.newKeySet();
		this.sleepTimeMultiplier = 1;
	}

	public AlgorithmEventHistoryPuller(AlgorithmEventHistory eventHistory) {
		this(eventHistory, 30);
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
		while (true) {
			if (!paused && timestep < eventHistory.getLength()) {
				AlgorithmEvent algorithmEvent = eventHistory.getEntryAtTimeStep(timestep).getAlgorithmEvent();
				sendAlgorithmEventToListeners(algorithmEvent);
				timestep++;
			}
			try {
				sleep((int) (sleepTimeMultiplier * maximumSleepTimeInMilliseconds));
			} catch (InterruptedException e) {
				// TODO handle this
			}
		}
	}

	private void sendAlgorithmEventToListeners(AlgorithmEvent algorithmEvent) {
		for (AlgorithmEventListener eventListener : algorithmEventListeners) {
			try {
				eventListener.handleAlgorithmEvent(algorithmEvent);
			} catch (HandleAlgorithmEventException e) {
				// TODO LOG THIS ERROR
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
			pause();
		} else if (guiEvent instanceof GoToTimeStepEvent) {
			handleGoToTimeStepEvent(guiEvent);
		} else if (guiEvent instanceof ChangeSpeedEvent) {
			handleChangeSpeedEvent(guiEvent);
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

	private void handleGoToTimeStepEvent(GUIEvent guiEvent) {
		resetTimeStep();
		GoToTimeStepEvent goToTimeStepEvent = (GoToTimeStepEvent) guiEvent;
		while (timestep < goToTimeStepEvent.getNewTimeStep() && timestep < eventHistory.getLength()) {
			AlgorithmEvent algorithmEvent = eventHistory.getEntryAtTimeStep(timestep).getAlgorithmEvent();
			sendAlgorithmEventToListeners(algorithmEvent);
			timestep++;
		}
	}

	private void handleChangeSpeedEvent(GUIEvent guiEvent) {
		ChangeSpeedEvent changeSpeedEvent = (ChangeSpeedEvent) guiEvent;
		sleepTimeMultiplier = 1 - changeSpeedEvent.getNewSpeedPercentage() / 100.0;
	}

}

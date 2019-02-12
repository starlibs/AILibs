package jaicore.graphvisualizer.events.recorder;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private Logger logger = LoggerFactory.getLogger(AlgorithmEventHistoryPuller.class);

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
		logger.info("AlgorithmEventHistoryPuller started with thread " + this.getName());
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
			if (paused)
				logger.debug("Not processing events since visualization is paused.");
			else if (timestep >= eventHistory.getLength())
				logger.debug("Not processing events since no unpublished events are known.");
			else {
				AlgorithmEventHistoryEntry historyEntry = eventHistory.getEntryAtTimeStep(timestep);
				AlgorithmEvent algorithmEvent = historyEntry.getAlgorithmEvent();
				logger.debug("Pulled event entry {} associated with event {} at position {}.", historyEntry, algorithmEvent, timestep);
				try {
					sendAlgorithmEventToListeners(algorithmEvent);
					logger.info("Pulled and sent event {} as entry at time step {}.", algorithmEvent, timestep);
					timestep++;
					int sleepTime = (int) (sleepTimeMultiplier * maximumSleepTimeInMilliseconds);
					logger.trace("Sleeping {}ms.", sleepTime);
					sleep(sleepTime);

				} catch (InterruptedException e) {
					return;
				}
				catch (Throwable e) {
					logger.error("Could not dispatch event {} due to error.", algorithmEvent, e.toString());
				}
			}
		}
	}

	private void sendAlgorithmEventToListeners(AlgorithmEvent algorithmEvent) {
		for (AlgorithmEventListener eventListener : algorithmEventListeners) {
			try {
				logger.debug("Sending event {} to listener {}.", algorithmEvent, eventListener);
				long start = System.currentTimeMillis();
				eventListener.handleAlgorithmEvent(algorithmEvent);
				long dispatchTime = System.currentTimeMillis() - start;
				if (dispatchTime > 10)
					logger.warn("Dispatch time for event {} to listener {} took {}ms!", algorithmEvent, eventListener, dispatchTime);
			} catch (HandleAlgorithmEventException e) {
				logger.error("Error in dispatching event." + e.toString());
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

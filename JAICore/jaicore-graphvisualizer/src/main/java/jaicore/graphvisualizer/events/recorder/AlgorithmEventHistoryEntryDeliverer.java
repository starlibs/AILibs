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

/**
 * The {@link AlgorithmEventHistoryEntryDeliverer} is {@link Thread} constantly pulling events from a given {@link AlgorithmEventHistory} and sending these to all registered
 * {@link AlgorithmEventListener}s.
 * 
 * @author ahetzer
 *
 */
public class AlgorithmEventHistoryEntryDeliverer extends Thread implements AlgorithmEventSource, GUIEventListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(AlgorithmEventHistoryEntryDeliverer.class);

	private Set<AlgorithmEventListener> algorithmEventListeners;
	private AlgorithmEventHistory eventHistory;
	private int maximumSleepTimeInMilliseconds;

	private int timestep;
	private boolean paused;
	private double sleepTimeMultiplier;

	public AlgorithmEventHistoryEntryDeliverer(AlgorithmEventHistory eventHistory, int maximumSleepTimeInMilliseconds) {
		this.eventHistory = eventHistory;
		this.maximumSleepTimeInMilliseconds = maximumSleepTimeInMilliseconds;

		this.timestep = 0;
		this.paused = true;
		this.algorithmEventListeners = ConcurrentHashMap.newKeySet();
		this.sleepTimeMultiplier = 1;

		LOGGER.info(getClass().getSimpleName() + " started with thread " + this.getName());
	}

	public AlgorithmEventHistoryEntryDeliverer(AlgorithmEventHistory eventHistory) {
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
				AlgorithmEventHistoryEntry historyEntry = eventHistory.getEntryAtTimeStep(timestep);
				AlgorithmEvent algorithmEvent = historyEntry.getAlgorithmEvent();
				LOGGER.debug("Pulled event entry {} associated with event {} at position {}.", historyEntry, algorithmEvent, timestep);

				sendAlgorithmEventToListeners(algorithmEvent);
				timestep++;
			} else if (paused) {
				LOGGER.debug("Not processing events since visualization is paused.");
			} else if (timestep >= eventHistory.getLength()) {
				LOGGER.debug("Not processing events since no unpublished events are known.");
			}

			goToSleep();
		}
	}

	private void goToSleep() {
		try {
			int sleepTime = (int) (sleepTimeMultiplier * maximumSleepTimeInMilliseconds);
			LOGGER.trace("Sleeping {}ms.", sleepTime);
			sleep(sleepTime);
		} catch (InterruptedException e) {
			LOGGER.info(getClass().getSimpleName() + " was interrupted due to exception: {}.", e);
		}
	}

	private void sendAlgorithmEventToListeners(AlgorithmEvent algorithmEvent) {
		for (AlgorithmEventListener eventListener : algorithmEventListeners) {
			try {
				sendAlgorithmEventToListener(algorithmEvent, eventListener);
			} catch (Throwable e) {
				LOGGER.error("Error in dispatching event {} due to error.", algorithmEvent, e.toString());
			}
		}
		LOGGER.info("Pulled and sent event {} as entry at time step {}.", algorithmEvent, timestep);
	}

	private void sendAlgorithmEventToListener(AlgorithmEvent algorithmEvent, AlgorithmEventListener eventListener) throws HandleAlgorithmEventException {
		LOGGER.debug("Sending event {} to listener {}.", algorithmEvent, eventListener);

		long startTime = System.currentTimeMillis();
		eventListener.handleAlgorithmEvent(algorithmEvent);
		long dispatchTime = System.currentTimeMillis() - startTime;

		if (dispatchTime > 10) {
			LOGGER.warn("Dispatch time for event {} to listener {} took {}ms!", algorithmEvent, eventListener, dispatchTime);
		}
	}

	@Override
	public void handleGUIEvent(GUIEvent guiEvent) {
		if (guiEvent instanceof PauseEvent) {
			pause();
		} else if (guiEvent instanceof PlayEvent) {
			unpause();
		} else if (guiEvent instanceof ResetEvent) {
			handleResetEvent();
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

	private void handleResetEvent() {
		resetTimeStep();
		pause();
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

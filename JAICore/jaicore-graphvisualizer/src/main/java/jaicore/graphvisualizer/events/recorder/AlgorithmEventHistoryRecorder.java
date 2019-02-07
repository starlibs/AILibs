package jaicore.graphvisualizer.events.recorder;

import com.google.common.eventbus.Subscribe;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.graphvisualizer.events.graph.bus.AlgorithmEventListener;

public class AlgorithmEventHistoryRecorder implements AlgorithmEventListener {

	private AlgorithmEventHistory graphEventHistory;

	public AlgorithmEventHistoryRecorder() {
		graphEventHistory = new AlgorithmEventHistory();
	}

	@Subscribe
	@Override
	public void handleAlgorithmEvent(AlgorithmEvent algorithmEvent) {
		synchronized (this) {
			graphEventHistory.addEvent(algorithmEvent);
		}
	}

	public AlgorithmEventHistory getHistory() {
		return graphEventHistory;
	}

}

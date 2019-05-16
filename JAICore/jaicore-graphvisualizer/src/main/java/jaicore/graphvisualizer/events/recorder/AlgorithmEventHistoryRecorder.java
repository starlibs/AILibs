package jaicore.graphvisualizer.events.recorder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.serializable.DefaultPropertyProcessedAlgorithmEvent;
import jaicore.basic.algorithm.events.serializable.PropertyProcessedAlgorithmEvent;
import jaicore.graphvisualizer.events.graph.bus.AlgorithmEventListener;
import jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import jaicore.graphvisualizer.events.recorder.property.PropertyComputationFailedException;

public class AlgorithmEventHistoryRecorder implements AlgorithmEventListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(AlgorithmEventHistoryRecorder.class);

	private AlgorithmEventHistory algorithmEventHistory;

	private List<AlgorithmEventPropertyComputer> eventPropertyComputers;

	public AlgorithmEventHistoryRecorder(List<AlgorithmEventPropertyComputer> eventPropertyComputers) {
		this.algorithmEventHistory = new AlgorithmEventHistory();
		this.eventPropertyComputers = eventPropertyComputers;
	}

	@Subscribe
	@Override
	public void handleAlgorithmEvent(AlgorithmEvent algorithmEvent) {
		synchronized (this) {
			PropertyProcessedAlgorithmEvent propertyProcessedAlgorithmEvent = convertAlgorithmEventToPropertyProcessedAlgorithmEvent(algorithmEvent);
			algorithmEventHistory.addEvent(propertyProcessedAlgorithmEvent);
		}
	}

	private PropertyProcessedAlgorithmEvent convertAlgorithmEventToPropertyProcessedAlgorithmEvent(AlgorithmEvent algorithmEvent) {
		Map<String, Object> properties = new HashMap<>();

		for (AlgorithmEventPropertyComputer algorithmEventPropertyComputer : eventPropertyComputers) {
			try {
				Object computedProperty = algorithmEventPropertyComputer.computeAlgorithmEventProperty(algorithmEvent);
				if (computedProperty != null) {
					properties.put(algorithmEventPropertyComputer.getPropertyName(), computedProperty);
				}
			} catch (PropertyComputationFailedException e) {
				LOGGER.error("Could not compute property \"{}\".", algorithmEventPropertyComputer.getPropertyName(), e);
			}
		}

		PropertyProcessedAlgorithmEvent serializableAlgorithmEvent = new DefaultPropertyProcessedAlgorithmEvent(algorithmEvent.getClass().getSimpleName(), properties, algorithmEvent);
		return serializableAlgorithmEvent;
	}

	public AlgorithmEventHistory getHistory() {
		return algorithmEventHistory;
	}

}

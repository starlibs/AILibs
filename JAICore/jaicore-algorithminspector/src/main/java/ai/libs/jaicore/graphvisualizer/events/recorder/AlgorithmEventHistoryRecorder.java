package ai.libs.jaicore.graphvisualizer.events.recorder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.api4.java.algorithm.events.AlgorithmEvent;
import org.api4.java.algorithm.events.serializable.DefaultPropertyProcessedAlgorithmEvent;
import org.api4.java.algorithm.events.serializable.PropertyProcessedAlgorithmEvent;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.graphvisualizer.events.graph.bus.AlgorithmEventListener;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.PropertyComputationFailedException;

/**
 * An {@link AlgorithmEventHistoryRecorder} is responsible for recording {@link AlgorithmEvent}s and storing them in the form of {@link PropertyProcessedAlgorithmEvent}s. For doing so it requires a list of
 * {@link AlgorithmEventPropertyComputer}s which can extract information from {@link AlgorithmEvent}s which in turn is stored in the {@link PropertyProcessedAlgorithmEvent}s.
 *
 * @author atornede
 *
 */
public class AlgorithmEventHistoryRecorder implements AlgorithmEventListener, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(AlgorithmEventHistoryRecorder.class);

	private AlgorithmEventHistory algorithmEventHistory;

	private List<AlgorithmEventPropertyComputer> eventPropertyComputers;

	/**
	 * Creates a new {@link AlgorithmEventHistoryRecorder} with the given {@link AlgorithmEventPropertyComputer}s.
	 *
	 * @param eventPropertyComputers A list of {@link AlgorithmEventPropertyComputer}s which can extract information from {@link AlgorithmEvent}s which in turn is stored in the {@link PropertyProcessedAlgorithmEvent}s.
	 */
	public AlgorithmEventHistoryRecorder(final List<AlgorithmEventPropertyComputer> eventPropertyComputers) {
		this.algorithmEventHistory = new AlgorithmEventHistory();
		this.eventPropertyComputers = eventPropertyComputers;
	}

	@Subscribe
	@Override
	public void handleAlgorithmEvent(final AlgorithmEvent algorithmEvent) {
		this.logger.info("Receiving AlgorithmEvent {}", algorithmEvent);
		synchronized (this) {
			this.logger.debug("Converting AlgorithmEvent {}", algorithmEvent);
			PropertyProcessedAlgorithmEvent propertyProcessedAlgorithmEvent = this.convertAlgorithmEventToPropertyProcessedAlgorithmEvent(algorithmEvent);
			this.algorithmEventHistory.addEvent(propertyProcessedAlgorithmEvent);
		}
	}

	private PropertyProcessedAlgorithmEvent convertAlgorithmEventToPropertyProcessedAlgorithmEvent(final AlgorithmEvent algorithmEvent) {
		Map<String, Object> properties = new HashMap<>();
		for (AlgorithmEventPropertyComputer algorithmEventPropertyComputer : this.eventPropertyComputers) {
			try {
				Object computedProperty = algorithmEventPropertyComputer.computeAlgorithmEventProperty(algorithmEvent);
				this.logger.trace("Computer {} converted event to {}", algorithmEventPropertyComputer, computedProperty);
				if (computedProperty != null) {
					properties.put(algorithmEventPropertyComputer.getPropertyName(), computedProperty);
				}
			} catch (PropertyComputationFailedException e) {
				this.logger.error("Could not compute property \"{}\".", algorithmEventPropertyComputer.getPropertyName(), e);
			}
		}

		return new DefaultPropertyProcessedAlgorithmEvent(algorithmEvent.getClass().getSimpleName(), properties, algorithmEvent, algorithmEvent.getTimestamp());
	}

	/**
	 * Returns the {@link AlgorithmEventHistory} which is produced up to this point by this {@link AlgorithmEventHistoryRecorder}.
	 *
	 * @return The {@link AlgorithmEventHistory} which is produced up to this point by this {@link AlgorithmEventHistoryRecorder}.
	 */
	public AlgorithmEventHistory getHistory() {
		return this.algorithmEventHistory;
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}

}

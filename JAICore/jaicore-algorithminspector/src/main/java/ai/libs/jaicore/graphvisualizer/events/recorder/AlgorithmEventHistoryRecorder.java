package ai.libs.jaicore.graphvisualizer.events.recorder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.events.serializable.DefaultPropertyProcessedAlgorithmEvent;
import org.api4.java.algorithm.events.serializable.IPropertyProcessedAlgorithmEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.graphvisualizer.events.graph.bus.AlgorithmEventListener;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.PropertyComputationFailedException;

/**
 * An {@link AlgorithmEventHistoryRecorder} is responsible for recording {@link IAlgorithmEvent}s and storing them in the form of {@link IPropertyProcessedAlgorithmEvent}s. For doing so it requires a list of
 * {@link AlgorithmEventPropertyComputer}s which can extract information from {@link IAlgorithmEvent}s which in turn is stored in the {@link IPropertyProcessedAlgorithmEvent}s.
 *
 * @author atornede
 *
 */
public class AlgorithmEventHistoryRecorder implements AlgorithmEventListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(AlgorithmEventHistoryRecorder.class);

	private AlgorithmEventHistory algorithmEventHistory;

	private final List<AlgorithmEventPropertyComputer> eventPropertyComputers = new ArrayList<>();

	public AlgorithmEventHistoryRecorder() {
		this.algorithmEventHistory = new AlgorithmEventHistory();
	}

	/**
	 * Creates a new {@link AlgorithmEventHistoryRecorder} with the given {@link AlgorithmEventPropertyComputer}s.
	 *
	 * @param eventPropertyComputers A list of {@link AlgorithmEventPropertyComputer}s which can extract information from {@link IAlgorithmEvent}s which in turn is stored in the {@link IPropertyProcessedAlgorithmEvent}s.
	 */
	public AlgorithmEventHistoryRecorder(final List<AlgorithmEventPropertyComputer> eventPropertyComputers) {
		this();
		this.eventPropertyComputers.addAll(eventPropertyComputers);
	}

	public boolean hasPropertyComputerInstalled(final AlgorithmEventPropertyComputer propertyComputer) {
		return this.eventPropertyComputers.stream().anyMatch(pc -> pc.getClass().equals(propertyComputer.getClass()));
	}

	public AlgorithmEventPropertyComputer getInstalledCopyOfPropertyComputer(final AlgorithmEventPropertyComputer propertyComputer) {
		return this.eventPropertyComputers.stream().filter(pc -> pc.getClass().equals(propertyComputer.getClass())).findAny().get();
	}

	public void addPropertyComputer(final Collection<AlgorithmEventPropertyComputer> computers) {
		for (AlgorithmEventPropertyComputer computer : computers) {
			if (this.hasPropertyComputerInstalled(computer)) {
				LOGGER.info("Not adding a second instance of property computer {}. One is already installed. Make sure that they are not computing semantically different things.", computer.getClass().getName());
			}
			else {

				/* first extend property computer graph if necessary or overwrite the required property computers bythe existing ones */
				for (AlgorithmEventPropertyComputer requiredComputer : computer.getRequiredPropertyComputers()) {
					if (this.hasPropertyComputerInstalled(requiredComputer)) {
						computer.overwriteRequiredPropertyComputer(this.getInstalledCopyOfPropertyComputer(requiredComputer));
					}
					else {
						this.addPropertyComputer(requiredComputer);
					}
				}

				/* check required other computers and if they have already been installed */
				this.eventPropertyComputers.add(computer);
			}
		}
	}

	public void addPropertyComputer(final AlgorithmEventPropertyComputer... computer) {
		Collections.addAll(this.eventPropertyComputers, computer);
	}

	@Subscribe
	@Override
	public void handleAlgorithmEvent(final IAlgorithmEvent algorithmEvent) {
		synchronized (this) {
			IPropertyProcessedAlgorithmEvent propertyProcessedAlgorithmEvent = this.convertAlgorithmEventToPropertyProcessedAlgorithmEvent(algorithmEvent);
			this.algorithmEventHistory.addEvent(propertyProcessedAlgorithmEvent);
		}
	}

	private IPropertyProcessedAlgorithmEvent convertAlgorithmEventToPropertyProcessedAlgorithmEvent(final IAlgorithmEvent algorithmEvent) {
		Map<String, Object> properties = new HashMap<>();

		for (AlgorithmEventPropertyComputer algorithmEventPropertyComputer : this.eventPropertyComputers) {
			try {
				Object computedProperty = algorithmEventPropertyComputer.computeAlgorithmEventProperty(algorithmEvent);
				if (computedProperty != null) {
					properties.put(algorithmEventPropertyComputer.getPropertyName(), computedProperty);
				}
			} catch (PropertyComputationFailedException e) {
				LOGGER.error("Could not compute property \"{}\".", algorithmEventPropertyComputer.getPropertyName(), e);
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

}

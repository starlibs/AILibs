package jaicore.ml.cache;

import com.google.common.eventbus.EventBus;

/**
 * Listens to all Test, Selection, and Validation-Split events and stores a
 * trajectory internally.
 * 
 *  Additionally, it can perform a database lookup on the
 * trajectory and return the cached result from the database.
 * 
 * @author mirko
 *
 */
public class ReproducibilityListener<A, B, C, D, E, F> {
	
	/**
	 * The event bus where the trajectory events arrive.
	 */
	private final EventBus reproducibilityEventBus;

	private DatabaseAdapter database;
	
	public ReproducibilityListener(EventBus reproducibilityEventBus) {
		this.reproducibilityEventBus = reproducibilityEventBus;
	}
	
	
	
}

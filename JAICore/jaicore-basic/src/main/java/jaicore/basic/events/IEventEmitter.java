package jaicore.basic.events;

/**
 * Interface for event emitting classes, allowing other objects to register themselves as listener.
 * Basic event infrastructure is served by Google's library guava and the contained EventBus.
 *
 * @author fmohr, mwever
 */
public interface IEventEmitter {

	/**
	 * Registers the provided object as a listener on the internal event bus such that the registered listener is supplied with emitted events.
	 *
	 * @param listener The listener to be registered on the event bus system.
	 */
	public void registerListener(Object listener);

}

package jaicore.graphvisualizer.guiOld.dataSupplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.eventbus.EventBus;

import jaicore.graphvisualizer.events.controlEvents.ControlEvent;
import jaicore.graphvisualizer.events.graphEvents.GraphEvent;

/**
 * This interface describes a supplier, which computes data. The data is send out over an eventbus and received by an IVisualizer.
 * @author jkoepe
 *
 */
public interface ISupplier {

	/**
	 * Register an listener to this supplier. In most cases this will be an IVisualizer
	 * @param listener
	 */
    void registerListener(Object listener);

    /**
     * This function computes what is happening if a VisuEvent is incoming.
     * @param event
     * 		The received VisuEvent
     */
    void receiveGraphEvent(GraphEvent event);
    
    /**
     * This function describes what is happening, if an ControlEvent is incoming. Usually these events will start at a FXController.
     * @param event
     */
    void receiveControlEvent(ControlEvent event);

    /**
     * Returns a Jsonnode, which contains this serialization.
     * This Jasonnode can be used to recreate the Supplier.
     * @return
     *		A JsonNode containing information of the supplier.
     */
    JsonNode getSerialization();



}

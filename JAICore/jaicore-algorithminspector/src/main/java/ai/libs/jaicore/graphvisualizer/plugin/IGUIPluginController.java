package ai.libs.jaicore.graphvisualizer.plugin;

import org.api4.java.algorithm.events.serializable.IPropertyProcessedAlgorithmEvent;

import ai.libs.jaicore.graphvisualizer.events.gui.GUIEvent;
import ai.libs.jaicore.graphvisualizer.events.gui.GUIEventListener;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.PropertyProcessedAlgorithmEventListener;

/**
 * An {@link IGUIPluginController} is part of an {@link IGUIPlugin} and is reponsible for handling {@link IPropertyProcessedAlgorithmEvent}s and {@link GUIEvent}s it is provided. This usually involves either directly reacting to these
 * events, e.g. a mouse click, or extract required information from a {@link IPropertyProcessedAlgorithmEvent} and store it inside a {@link IGUIPluginModel}.
 *
 * @author atornede
 *
 */
public interface IGUIPluginController extends PropertyProcessedAlgorithmEventListener, GUIEventListener {

}

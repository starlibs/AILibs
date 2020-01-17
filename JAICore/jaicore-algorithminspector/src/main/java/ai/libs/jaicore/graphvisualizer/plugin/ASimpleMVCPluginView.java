package ai.libs.jaicore.graphvisualizer.plugin;

import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.Node;

/**
 *
 * @author fmohr
 *
 */
public abstract class ASimpleMVCPluginView<M extends IGUIPluginModel, C extends IGUIPluginController, N extends Node> implements IGUIPluginView, ILoggingCustomizable {

	private final N node;
	private final M model;
	private C controller;

	protected Logger logger = LoggerFactory.getLogger("gui.view." + this.getClass().getName());

	public ASimpleMVCPluginView(final M model, final N node) {
		this.model = model;
		this.node = node;
	}

	public M getModel() {
		return this.model;
	}

	public C getController() {
		return this.controller;
	}

	public void setController(final C controller) {
		this.controller = controller;
	}

	public abstract void clear();

	/**
	 * Gets the node that represents this view
	 */
	@Override
	public N getNode() {
		return this.node;
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

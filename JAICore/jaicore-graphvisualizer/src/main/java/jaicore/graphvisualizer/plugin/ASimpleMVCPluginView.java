package jaicore.graphvisualizer.plugin;

import javafx.scene.Node;

/**
 * 
 * @author fmohr
 *
 */
public abstract class ASimpleMVCPluginView<M extends IGUIPluginModel, C extends IGUIPluginController, N extends Node> implements IGUIPluginView {

	private final N node;
	private final M model;
	private C controller;

	public ASimpleMVCPluginView(M model, N node) {
		this.model = model;
		this.node = node;
	}

	public M getModel() {
		return model;
	}

	public C getController() {
		return controller;
	}

	public void setController(C controller) {
		this.controller = controller;
	}

	public abstract void clear();

	/**
	 * Gets the node that represents this view
	 */
	public N getNode() {
		return node;
	}
}

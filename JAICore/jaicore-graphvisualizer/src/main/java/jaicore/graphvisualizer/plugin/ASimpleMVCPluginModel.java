package jaicore.graphvisualizer.plugin;

import jaicore.graphvisualizer.plugin.IGUIPluginModel;

/**
 * 
 * @author fmohr
 *
 */
public abstract class ASimpleMVCPluginModel<V extends IGUIPluginView, C extends IGUIPluginController> implements IGUIPluginModel {

	private V view;
	private C controller;

	public void setView(V view) {
		this.view = view;
	}

	public V getView() {
		return view;
	}

	public C getController() {
		return controller;
	}

	public void setController(C controller) {
		this.controller = controller;
	}
}

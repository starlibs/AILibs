package jaicore.graphvisualizer.plugin;

/**
 * 
 * @author fmohr
 *
 */
public abstract class ASimpleMVCPluginModel<V extends ASimpleMVCPluginView<?, ?, ?>, C extends ASimpleMVCPluginController<?,?>> implements IGUIPluginModel {

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
	
	public abstract void clear();
}

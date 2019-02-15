package jaicore.graphvisualizer.plugin;

/**
 * 
 * @author fmohr
 *
 */
public abstract class ASimpleMVCPluginView<M extends IGUIPluginModel, C extends IGUIPluginController> implements IGUIPluginView {

	private final M model;
	private C controller;
	
	public ASimpleMVCPluginView(M model) {
		this.model = model;
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
}

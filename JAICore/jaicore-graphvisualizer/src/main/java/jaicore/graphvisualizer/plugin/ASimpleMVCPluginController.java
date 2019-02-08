package jaicore.graphvisualizer.plugin;

public abstract class ASimpleMVCPluginController<M extends IGUIPluginModel, V extends IGUIPluginView> implements IGUIPluginController {
	private final V view;
	private final M model;

	public ASimpleMVCPluginController(M model, V view) {
		super();
		this.model = model;
		this.view = view;
	}

	public M getModel() {
		return model;
	}

	public V getView() {
		return view;
	}
}

package jaicore.graphvisualizer.plugin;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.graphvisualizer.events.graph.bus.AlgorithmEventSource;
import jaicore.graphvisualizer.events.gui.GUIEventSource;

public abstract class ASimpleMVCPlugin<M extends ASimpleMVCPluginModel<V, C>, V extends ASimpleMVCPluginView<M, C, ?>, C extends ASimpleMVCPluginController<M, V>> implements IGUIPlugin {

	private final Logger logger = LoggerFactory.getLogger(ASimpleMVCPlugin.class);
	private final M model;
	private final V view;
	private final C controller;

	@SuppressWarnings("unchecked")
	public ASimpleMVCPlugin() {
		super();
		Type[] mvcPatternClasses = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments();
		M myModel;
		V myView;
		C myController;
		try {
			if (this.logger.isDebugEnabled()) {
				this.logger.debug(mvcPatternClasses[0].getTypeName().replaceAll("(<.*>)", ""));
			}

			Class<M> modelClass = ((Class<M>) Class.forName(this.getClassNameWithoutGenerics(mvcPatternClasses[0].getTypeName())));
			Class<V> viewClass = ((Class<V>) Class.forName(this.getClassNameWithoutGenerics(mvcPatternClasses[1].getTypeName())));
			Class<C> controllerClass = ((Class<C>) Class.forName(this.getClassNameWithoutGenerics(mvcPatternClasses[2].getTypeName())));
			myModel = modelClass.newInstance();
			myView = viewClass.getDeclaredConstructor(modelClass).newInstance(myModel);
			myController = controllerClass.getDeclaredConstructor(modelClass, viewClass).newInstance(myModel, myView);
			myController.start();
		} catch (Exception e) {
			this.logger.error("Could not initialize {} due to exception in building MVC.", this, e);
			this.model = null;
			this.view = null;
			this.controller = null;
			return;
		}
		this.model = myModel;
		this.view = myView;
		this.controller = myController;
		this.model.setController(myController);
		this.model.setView(myView);
		this.view.setController(myController);
	}

	@Override
	public C getController() {
		return this.controller;
	}

	@Override
	public M getModel() {
		return this.view.getModel();
	}

	@Override
	public V getView() {
		return this.view;
	}

	@Override
	public void setAlgorithmEventSource(final AlgorithmEventSource graphEventSource) {
		graphEventSource.registerListener(this.controller);
	}

	@Override
	public void setGUIEventSource(final GUIEventSource guiEventSource) {
		guiEventSource.registerListener(this.controller);
	}

	private String getClassNameWithoutGenerics(final String className) {
		return className.replaceAll("(<.*>)", "");
	}
}

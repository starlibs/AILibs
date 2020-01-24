package ai.libs.jaicore.graphvisualizer.plugin;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.graphvisualizer.events.gui.GUIEventSource;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.PropertyProcessedAlgorithmEventSource;

public abstract class ASimpleMVCPlugin<M extends ASimpleMVCPluginModel<V, C>, V extends ASimpleMVCPluginView<M, C, ?>, C extends ASimpleMVCPluginController<M, V>> implements IComputedGUIPlugin, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(ASimpleMVCPlugin.class);
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
			myModel = modelClass.getConstructor().newInstance();
			myView = viewClass.getDeclaredConstructor(modelClass).newInstance(myModel);
			myController = controllerClass.getDeclaredConstructor(modelClass, viewClass).newInstance(myModel, myView);
			myController.setDaemon(true);
			myController.start();
		} catch (Exception e) {
			this.logger.error("Could not initialize {} due to exception in building MVC.", this, e);
			this.model = null;
			this.view = null;
			this.controller = null;
			return;
		}
		Objects.requireNonNull(myController);
		Objects.requireNonNull(myView);
		Objects.requireNonNull(myModel);
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
	public void setAlgorithmEventSource(final PropertyProcessedAlgorithmEventSource algorithmEventSource) {
		Objects.requireNonNull(this.controller);
		algorithmEventSource.registerListener(this.controller);
	}

	@Override
	public void setGUIEventSource(final GUIEventSource guiEventSource) {
		guiEventSource.registerListener(this.controller);
	}

	private String getClassNameWithoutGenerics(final String className) {
		return className.replaceAll("(<.*>)", "");
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		this.model.setLoggerName(name + ".model");
		this.view.setLoggerName(name + ".view");
		this.controller.setLoggerName(name + ".controller");
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void stop() {
		this.logger.info("Interrupting controller thread {}", this.controller.getName());
		this.controller.interrupt();
	}
}

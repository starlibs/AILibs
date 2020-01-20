package ai.libs.jaicore.graphvisualizer.plugin;

import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author fmohr
 *
 */
public abstract class ASimpleMVCPluginModel<V extends ASimpleMVCPluginView<?, ?, ?>, C extends ASimpleMVCPluginController<?,?>> implements IGUIPluginModel, ILoggingCustomizable {

	private V view;
	private C controller;
	protected Logger logger = LoggerFactory.getLogger("gui.model." + this.getClass().getName());

	public void setView(final V view) {
		this.view = view;
	}

	public V getView() {
		return this.view;
	}

	public C getController() {
		return this.controller;
	}

	public void setController(final C controller) {
		this.controller = controller;
	}

	public abstract void clear();


	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}
}

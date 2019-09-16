package ai.libs.jaicore.graphvisualizer.plugin.nodeinfo;

import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.graphvisualizer.plugin.IGUIPluginView;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 *
 * @author hetzer
 *
 * @param <N>
 *            The node class
 */
public class NodeInfoGUIPluginView implements IGUIPluginView, ILoggingCustomizable {

	private NodeInfoGUIPluginModel model;

	private String title;

	private WebEngine webViewEngine;

	private Logger logger;

	public NodeInfoGUIPluginView(final String title) {
		this.model = new NodeInfoGUIPluginModel(this);
		this.title = title;
	}

	public NodeInfoGUIPluginView() {
		this("Node Info View");
	}

	@Override
	public Node getNode() {
		WebView htmlView = new WebView();
		this.webViewEngine = htmlView.getEngine();

		this.webViewEngine.loadContent("<i>No node selected</i>");

		return htmlView;
	}

	@Override
	public void update() {
		String nodeInfoOfCurrentlySelectedNode = this.model.getNodeInfoForCurrentlySelectedNode();
		Platform.runLater(() -> {
			this.webViewEngine.loadContent(nodeInfoOfCurrentlySelectedNode);
		});
	}

	public NodeInfoGUIPluginModel getModel() {
		return this.model;
	}

	@Override
	public String getTitle() {
		return this.title;
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

package ai.libs.jaicore.graphvisualizer.plugin.nodeinfo;

import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginView;
import javafx.application.Platform;
import javafx.scene.layout.FlowPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 *
 * @author hetzer
 *
 * @param <N>
 *            The node class
 */
public class NodeInfoGUIPluginView extends ASimpleMVCPluginView<NodeInfoGUIPluginModel, NodeInfoGUIPluginController, FlowPane> {

	private NodeInfoGUIPluginModel model;

	private String title;

	private WebEngine webViewEngine;

	public NodeInfoGUIPluginView(final NodeInfoGUIPluginModel model) {
		super (model, new FlowPane());
		WebView view = new WebView();
		FlowPane node = this.getNode();
		node.getChildren().add(view);
		this.webViewEngine = view.getEngine();
		this.webViewEngine.loadContent("<i>No node selected</i>");
	}


	@Override
	public void update() {
		String nodeInfoOfCurrentlySelectedNode = this.model.getNodeInfoForCurrentlySelectedNode();
		Platform.runLater(() -> this.webViewEngine.loadContent(nodeInfoOfCurrentlySelectedNode));
	}

	@Override
	public NodeInfoGUIPluginModel getModel() {
		return this.model;
	}

	@Override
	public String getTitle() {
		return this.title;
	}


	@Override
	public void clear() {
		/* don't do anything */
	}
}

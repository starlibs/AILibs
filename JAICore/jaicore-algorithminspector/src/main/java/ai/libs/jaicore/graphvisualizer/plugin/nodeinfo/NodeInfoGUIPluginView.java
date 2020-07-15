package ai.libs.jaicore.graphvisualizer.plugin.nodeinfo;

import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginView;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 *
 * @author Felix Mohr
 *
 */
public class NodeInfoGUIPluginView extends ASimpleMVCPluginView<NodeInfoGUIPluginModel, NodeInfoGUIPluginController, StackPane> {

	private WebEngine webViewEngine;

	public NodeInfoGUIPluginView(final NodeInfoGUIPluginModel model) {
		super(model, new StackPane());
		Platform.runLater(() -> {
			WebView view = new WebView();
			StackPane node = this.getNode();
			node.getChildren().add(view);
			this.webViewEngine = view.getEngine();
			this.webViewEngine.loadContent("<i>No node selected</i>");
		});
	}

	@Override
	public void update() {
		String nodeInfoOfCurrentlySelectedNode = this.getModel().getNodeInfoForCurrentlySelectedNode();
		Platform.runLater(() -> this.webViewEngine.loadContent(nodeInfoOfCurrentlySelectedNode));
	}

	@Override
	public String getTitle() {
		return "Node Information";
	}

	@Override
	public void clear() {
		/* don't do anything */
	}
}

package ai.libs.jaicore.graphvisualizer.plugin.nodeinfo;

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
public class NodeInfoGUIPluginView implements IGUIPluginView {

	private NodeInfoGUIPluginModel model;

	private String title;

	private WebEngine webViewEngine;

	public NodeInfoGUIPluginView(String title) {
		this.model = new NodeInfoGUIPluginModel(this);
		this.title = title;
	}

	public NodeInfoGUIPluginView() {
		this("Node Info View");
	}

	@Override
	public Node getNode() {
		WebView htmlView = new WebView();
		webViewEngine = htmlView.getEngine();

		webViewEngine.loadContent("<i>No node selected</i>");

		return htmlView;
	}

	@Override
	public void update() {
		String nodeInfoOfCurrentlySelectedNode = model.getNodeInfoForCurrentlySelectedNode();
		Platform.runLater(() -> {
			webViewEngine.loadContent(nodeInfoOfCurrentlySelectedNode);
		});
	}

	public NodeInfoGUIPluginModel getModel() {
		return model;
	}

	@Override
	public String getTitle() {
		return title;
	}

}

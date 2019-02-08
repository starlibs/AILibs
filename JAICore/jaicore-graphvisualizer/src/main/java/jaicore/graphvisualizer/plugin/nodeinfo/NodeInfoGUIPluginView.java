package jaicore.graphvisualizer.plugin.nodeinfo;

import jaicore.graphvisualizer.plugin.IGUIPluginView;
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
public class NodeInfoGUIPluginView<N> implements IGUIPluginView {

	private NodeInfoGUIPluginModel<N> model;
	private NodeInfoGenerator<N> nodeInfoGenerator;

	private WebEngine webViewEngine;

	public NodeInfoGUIPluginView(NodeInfoGenerator<N> nodeInfoGenerator) {
		this.model = new NodeInfoGUIPluginModel<>(this);
		this.nodeInfoGenerator = nodeInfoGenerator;
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
		N currentlySelectedNode = model.getCurrentlySelectedNode();
		String nodeInfoOfCurrentlySelectedNode = nodeInfoGenerator.generateInfoForNode(currentlySelectedNode);
		Platform.runLater(() -> {
			webViewEngine.loadContent(nodeInfoOfCurrentlySelectedNode);
		});
	}

	public NodeInfoGUIPluginModel<N> getModel() {
		return model;
	}

	@Override
	public String getTitle() {
		return "Node Info";
	}

}

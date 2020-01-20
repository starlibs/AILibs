package ai.libs.jaicore.search.gui.plugins.mcts.bradleyterry;

import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ai.libs.jaicore.graphvisualizer.events.gui.DefaultGUIEventBus;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginView;
import ai.libs.jaicore.graphvisualizer.plugin.graphview.NodeClickedEvent;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 *
 * @author fmohr
 *
 * @param <BalancedTreeNode>
 *            The node class
 */
public class BradleyTerryPluginView extends ASimpleMVCPluginView<BradleyTerryPluginModel, BradleyTerryPluginController, FlowPane> {

	private final Button left = new Button("left");
	private final Button right = new Button("right");
	private final Button parent = new Button("parent");
	private WebEngine engine;

	private static final String HTML_TD_OPEN = "<td>";
	private static final String HTML_TD_CLOSE = "</td>";
	private static final String HTML_TR_OPEN = "<tr>";
	private static final String HTML_TR_CLOSE = "</tr>";

	public BradleyTerryPluginView(final BradleyTerryPluginModel model) {
		super(model, new FlowPane());
		Platform.runLater(() -> {
			WebView view = new WebView();
			FlowPane node = this.getNode();
			node.getChildren().add(this.left);
			node.getChildren().add(this.right);
			node.getChildren().add(this.parent);
			this.left.setOnMouseClicked(e -> {
				DefaultGUIEventBus.getInstance().postEvent(new NodeClickedEvent(null, this.getLeftChild(model.getCurrentlySelectedNode())));
				this.parent.setDisable(false);
			});
			this.right.setOnMouseClicked(e -> {
				DefaultGUIEventBus.getInstance().postEvent(new NodeClickedEvent(null, this.getRightChild(model.getCurrentlySelectedNode())));
				this.parent.setDisable(false);
			});
			this.parent.setOnMouseClicked(e -> {
				String parentOfCurrent = this.getModel().getParentOfCurrentNode();
				DefaultGUIEventBus.getInstance().postEvent(new NodeClickedEvent(null, parentOfCurrent));
				if (!this.getModel().getParents().containsKey(parentOfCurrent)) {
					this.parent.setDisable(true);
				}
			});

			node.getChildren().add(view);
			this.engine = view.getEngine();
			this.engine.loadContent("Nothing there yet.");
		});
	}

	private String getLeftChild(final String node) {
		return this.getModel().getListsOfKnownSuccessors().get(node).get(0);
	}

	private String getRightChild(final String node) {
		return this.getModel().getListsOfKnownSuccessors().get(node).size() > 1 ? this.getModel().getListsOfKnownSuccessors().get(node).get(1) : null;
	}

	@Override
	public void update() {
		StringBuilder sb = new StringBuilder();
		sb.append("<h2>Analysis of node ");
		String currentNode = this.getModel().getCurrentlySelectedNode();
		sb.append(currentNode);
		sb.append(" (depth ");
		Map<String, String> parents = this.getModel().getParents();
		int depth = 0;
		while (parents.containsKey(currentNode)) {
			currentNode = parents.get(currentNode);
			depth ++;
		}
		sb.append(depth);
		sb.append(")</h2>");
		BradleyTerryUpdate update = this.getModel().getUpdateOfSelectedNode();
		if (update != null) {
			sb.append("<p>Number of visits: ");
			sb.append(update.getVisits());
			sb.append("</p>");
			sb.append("<h2>Stats of children</h2><table><tr>");

			/* first row contains number of visits */
			BradleyTerryUpdate modelOfLeftChild = this.getModel().getBtUpdates().get(this.getLeftChild(update.getNode()));
			BradleyTerryUpdate modelOfRightChild = this.getModel().getBtUpdates().get(this.getRightChild(update.getNode()));
			sb.append(HTML_TD_OPEN);
			sb.append(modelOfLeftChild != null ? modelOfLeftChild.getVisits() : 0);
			sb.append(HTML_TD_CLOSE);
			sb.append(HTML_TD_OPEN);
			sb.append(modelOfRightChild != null ? modelOfRightChild.getVisits() : 0);
			sb.append(HTML_TD_CLOSE);

			/* second row contains wins */
			sb.append(HTML_TR_CLOSE);
			sb.append(HTML_TR_OPEN);
			sb.append(HTML_TD_OPEN);
			sb.append(update.getWinsLeft());
			sb.append(HTML_TD_CLOSE);
			sb.append(HTML_TD_OPEN);
			sb.append(update.getWinsRight());
			sb.append(HTML_TD_CLOSE);

			/* third row contains probabilities */
			sb.append(HTML_TR_CLOSE);
			sb.append(HTML_TR_OPEN);
			sb.append(HTML_TD_OPEN);
			sb.append(update.getpLeftScaled());
			sb.append(" (");
			sb.append(update.getpLeft());
			sb.append(")</td>");
			sb.append(HTML_TD_OPEN);
			sb.append(update.getpRightScaled());
			sb.append(" (");
			sb.append(update.getpRight());
			sb.append(")</td>");

			/* fourth row contains stats summary */
			sb.append(HTML_TR_CLOSE);
			sb.append(HTML_TR_OPEN);
			DescriptiveStatistics leftStats = new DescriptiveStatistics();
			update.getScoresLeft().forEach(leftStats::addValue);
			DescriptiveStatistics rightStats = new DescriptiveStatistics();
			update.getScoresRight().forEach(rightStats::addValue);
			sb.append(HTML_TD_OPEN);
			sb.append(leftStats.toString().replace("\n", "<br />"));
			sb.append(HTML_TD_CLOSE);
			sb.append(HTML_TD_OPEN);
			sb.append(rightStats.toString().replace("\n", "<br />"));
			sb.append(HTML_TD_CLOSE);

			/* third row contains lists of considers observations */
			sb.append("</tr><tr>");
			sb.append("<td style=\"vertical-align: top;\"><ul>");
			update.getScoresLeft().forEach(d -> sb.append("<li>" + d + "</li>"));
			sb.append("</ul></td>");
			sb.append("<td style=\"vertical-align: top;\"><ul>");
			update.getScoresRight().forEach(d -> sb.append("<li>" + d + "</li>"));
			sb.append("</ul></td>");
			sb.append("</tr></table>");
		}
		Platform.runLater(() -> this.engine.loadContent(sb.toString()));
	}

	@Override
	public void clear() {
		/* don't do anything */
	}

	@Override
	public String getTitle() {
		return "Search Rollout Statistics";
	}

}

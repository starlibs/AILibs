package ai.libs.jaicore.search.gui.plugins.mcts.dng;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerXYDataset;

import ai.libs.jaicore.basic.MathExt;
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
public class DNGMCTSPluginView extends ASimpleMVCPluginView<DNGMCTSPluginModel, DNGMCTSPluginController, FlowPane> {

	private final Button left = new Button("left");
	private final Button right = new Button("right");
	private final Button parent = new Button("parent");
	private WebEngine engine;
	private final BoxAndWhiskerXYDataset dataset = new DefaultBoxAndWhiskerXYDataset("plot");
	private final JFreeChart chart = ChartFactory.createBoxAndWhiskerChart("Box and Whisker Chart", "Time", "Value", this.dataset, true);

	public DNGMCTSPluginView(final DNGMCTSPluginModel model) {
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
			depth++;
		}
		sb.append(depth);
		sb.append(")</h2>");
		sb.append("<h3>Mu-Estimates of Children</h3>");
		String currentlySelectedNode = this.getModel().getCurrentlySelectedNode();
		String leftChild = this.getLeftChild(currentlySelectedNode);
		String rightChild = this.getRightChild(currentlySelectedNode);
		List<DNGBeliefUpdate> muValuesOfLeft = this.getModel().getObservedMuValues().get(leftChild);
		sb.append("<h4>" + leftChild + " (" + (muValuesOfLeft != null ? muValuesOfLeft.size() : "-1") + ")</h4>");
		if (muValuesOfLeft != null) {
			DNGBeliefUpdate latestUpdate = muValuesOfLeft.get(muValuesOfLeft.size() - 1);
			DescriptiveStatistics statsOfLeft = this.getModel().getObservationStatisticsOfNode(leftChild);
			sb.append("Mu: " + latestUpdate.getMu() + "<br />");
			sb.append("Mu - sampleMean: " + (latestUpdate.getMu() - statsOfLeft.getMean()) + "<br />");
			sb.append("Alpha: " + latestUpdate.getAlpha() + "<br />");
			sb.append("Beta: " + latestUpdate.getBeta() + "<br />");
			sb.append("Lambda: " + latestUpdate.getLambda());
		}
		if (rightChild != null) {
			List<DNGBeliefUpdate> muValuesOfRight = this.getModel().getObservedMuValues().get(rightChild);
			DescriptiveStatistics statsOfRight = this.getModel().getObservationStatisticsOfNode(rightChild);
			sb.append("<h4>" + rightChild + " (" + (muValuesOfRight != null ? muValuesOfRight.size() : "-1") + ")</h4>");
			if (muValuesOfRight != null) {
				DNGBeliefUpdate latestUpdate = muValuesOfRight.get(muValuesOfRight.size() - 1);
				sb.append("Mu: " + latestUpdate.getMu() + "<br />");
				sb.append("Mu - sampleMean: " + (latestUpdate.getMu() - statsOfRight.getMean()) + "<br />");
				sb.append("Alpha: " + latestUpdate.getAlpha() + "<br />");
				sb.append("Beta: " + latestUpdate.getBeta() + "<br />");
				sb.append("Lambda: " + latestUpdate.getLambda());
			}
		}
		sb.append("<h3>Q-Values of Children</h3>");
		Map<String, List<Double>> qValues = this.getModel().getQValuesOfSelectedNode();
		if (qValues != null) {
			{
				List<Double> scoresOfLeft = qValues.get(leftChild);
				sb.append("<h4>" + leftChild + " (" + scoresOfLeft.size()  + ")</h4>");
				sb.append(scoresOfLeft.subList(Math.max(0, scoresOfLeft.size() - 5), scoresOfLeft.size()).stream().map(v -> MathExt.round(v, 4)).collect(Collectors.toList()));
			}

			{
				List<Double> scoresOfRight = qValues.get(rightChild);
				if (scoresOfRight != null) {
					sb.append("<h4>" + rightChild + " (" + scoresOfRight.size()  + ")</h4>");
					sb.append(scoresOfRight.subList(Math.max(0, scoresOfRight.size() - 5), scoresOfRight.size()).stream().map(v -> MathExt.round(v, 4)).collect(Collectors.toList()));
				}
			}
		}

		Platform.runLater(() -> {
			this.engine.loadContent(sb.toString());
		});
	}

	@Override
	public void clear() {

	}

	@Override
	public String getTitle() {
		return "Search Rollout Statistics";
	}

}

package ai.libs.jaicore.search.gui.plugins.rolloutboxplots;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerXYDataset;

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
public class SearchRolloutBoxplotPluginView extends ASimpleMVCPluginView<SearchRolloutBoxplotPluginModel, SearchRolloutBoxplotPluginController, FlowPane> {

	private final Button left = new Button("left");
	private final Button right = new Button("right");
	private final Button parent = new Button("parent");

	//	private ObservableList<Double> leftScores = new ObservableListWrapper<>(new ArrayList<>());
	//	private ObservableList<Double> rightScores = new ObservableListWrapper<>(new ArrayList<>());
	//	private DescriptiveStatisticsTimelineView leftChart = new DescriptiveStatisticsTimelineView(this.leftScores);
	//	private DescriptiveStatisticsTimelineView rightChart = new DescriptiveStatisticsTimelineView(this.rightScores);

	private WebEngine engine;
	private final BoxAndWhiskerXYDataset dataset = new DefaultBoxAndWhiskerXYDataset("plot");
	private final JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
			"Box and Whisker Chart", "Time", "Value", this.dataset, true);

	public SearchRolloutBoxplotPluginView(final SearchRolloutBoxplotPluginModel model) {
		super(model, new FlowPane());
		Platform.runLater(() -> {
			WebView view = new WebView();
			FlowPane node = this.getNode();
			node.getChildren().add(view);
			this.engine = view.getEngine();
			this.engine.loadContent("Nothing there yet.");
			node.getChildren().add(this.left);
			node.getChildren().add(this.right);
			node.getChildren().add(this.parent);
			//			node.getChildren().add(this.leftChart);
			//			node.getChildren().add(this.rightChart);
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
		});
	}

	private String getLeftChild(final String node) {
		if (!this.getModel().getListsOfKnownSuccessors().containsKey(node)) {
			throw new IllegalArgumentException(node + " has no children in the known model!");
		}
		return this.getModel().getListsOfKnownSuccessors().get(node).get(0);
	}

	private String getRightChild(final String node) {
		return this.getModel().getListsOfKnownSuccessors().get(node).size() > 1 ? this.getModel().getListsOfKnownSuccessors().get(node).get(1) : null;
	}

	@Override
	public synchronized void update() {
		StringBuilder sb = new StringBuilder();
		DescriptiveStatistics stats = this.getModel().getObservedPerformancesUnderSelectedNode();
		if (stats != null) {
			sb.append(stats.toString().replace("\n", "<br />"));
			List<String> successors = this.getModel().getListOfKnownSuccessorsOfCurrentlySelectedNode();
			if (successors != null) {
				sb.append("<table><tr>");
				for (String successor : successors) {
					int index = successors.indexOf(successor);
					DescriptiveStatistics statsOfSuccessor = this.getModel().getObservedPerformances().get(successor);

					/* update table */
					sb.append("<td>");
					sb.append(statsOfSuccessor.toString().replace("\n", "<br />"));
					sb.append("</td>");

					/* update charts of mean values */
					//					ObservableList<Double> list = index == 0 ? this.leftScores : this.rightScores;
					//					double[] performanceValuesOfSuccessor = statsOfSuccessor.getValues();
					//					//					System.out.println(Arrays.toString(performanceValuesOfSuccessor) + " -> " + list);
					//					int m = performanceValuesOfSuccessor.length;
					//					int n = list.size();
					//					if (n > m) {
					//						throw new IllegalStateException("Cannot have more observations in view than in model! View has " + n + " points, model has " + m);
					//					}
					//					for (int i = n; i < m; i++) {
					//						list.add(performanceValuesOfSuccessor[i]);
					//					}
				}
				sb.append("</tr></table>");
			}
			Platform.runLater(() -> {
				this.engine.loadContent(sb.toString());
			});
		}
	}

	@Override
	public synchronized void clear() {
		//		this.leftScores.clear();
		//		this.rightScores.clear();
		//		System.out.println("CLEARED");
	}

	@Override
	public String getTitle() {
		return "Search Rollout Statistics";
	}

}

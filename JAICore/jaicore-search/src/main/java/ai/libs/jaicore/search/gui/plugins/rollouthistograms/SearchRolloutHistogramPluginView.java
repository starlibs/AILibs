package ai.libs.jaicore.search.gui.plugins.rollouthistograms;

import ai.libs.jaicore.graphvisualizer.events.gui.Histogram;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginView;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;

/**
 *
 * @author fmohr
 *
 * @param <BalancedTreeNode>
 *            The node class
 */
public class SearchRolloutHistogramPluginView extends ASimpleMVCPluginView<SearchRolloutHistogramPluginModel, SearchRolloutHistogramPluginController, StackPane> {

	private final Histogram histogram;
	private final int n = 100;

	public SearchRolloutHistogramPluginView(final SearchRolloutHistogramPluginModel model) {
		super(model, new StackPane());
		this.histogram = new Histogram(this.n);
		this.histogram.setTitle("Search Rollout Performances");
		Platform.runLater(() -> {
			this.getNode().getChildren().add(this.histogram);
		});
	}

	@Override
	public void update() {
		if (this.getModel().getCurrentlySelectedNode() != null && this.getModel().getObservedPerformancesUnderSelectedNode() != null) {
			Platform.runLater(() -> {
				this.histogram.update(this.getModel().getObservedPerformancesUnderSelectedNode());
			});
		}
	}

	@Override
	public void clear() {
		Platform.runLater(this.histogram::clear);
	}

}

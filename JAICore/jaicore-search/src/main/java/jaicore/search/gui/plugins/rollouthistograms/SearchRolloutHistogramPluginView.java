package jaicore.search.gui.plugins.rollouthistograms;

import jaicore.graphvisualizer.events.gui.Histogram;
import jaicore.graphvisualizer.plugin.ASimpleMVCPluginView;
import javafx.application.Platform;
import javafx.scene.layout.FlowPane;

/**
 * 
 * @author fmohr
 *
 * @param <N> The node class
 */
public class SearchRolloutHistogramPluginView<N>
		extends ASimpleMVCPluginView<SearchRolloutHistogramPluginModel<N>, SearchRolloutHistogramPluginController<N>, FlowPane> {

	private final Histogram histogram;
	private final int n = 100;

	public SearchRolloutHistogramPluginView(SearchRolloutHistogramPluginModel<N> model) {
		super(model, new FlowPane());
		histogram = new Histogram(n);
		histogram.setTitle("Search Rollout Performances");
		Platform.runLater(() -> {
			getNode().getChildren().add(histogram);
		});
	}

	@Override
	public void update() {
		if (getModel().getCurrentlySelectedNode() != null && getModel().getObservedPerformancesUnderSelectedNode() != null) {
			Platform.runLater(() -> {
				histogram.update(getModel().getObservedPerformancesUnderSelectedNode());
			});
		}
	}
	
	public void clear() {
		Platform.runLater(() -> {
			histogram.clear();
		});
	}

	@Override
	public String getTitle() {
		return "Search Rollout Statistics";
	}

}

package jaicore.search.gui.plugins.rollouthistograms;

import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import jaicore.graphvisualizer.events.gui.Histogram;
import jaicore.graphvisualizer.plugin.ASimpleMVCPluginView;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;

/**
 * 
 * @author fmohr
 *
 * @param <N>
 *            The node class
 */
public class SearchRolloutHistogramPluginView extends ASimpleMVCPluginView<SearchRolloutHistogramPluginModel, SearchRolloutHistogramPluginController> {

	private final Histogram histogram;
	private FlowPane root = new FlowPane();
    private final int n = 100;

    public SearchRolloutHistogramPluginView(SearchRolloutHistogramPluginModel model) {
		super(model);
		histogram = new Histogram(n);
		Platform.runLater(() -> {
			root.getChildren().add(histogram);
		});
	}	

	@Override
	public Node getNode() {
		return root;
	}

	@Override
	public void update() {
        Platform.runLater(() -> {
        	histogram.update(getHistogram(getModel().getObservedPerformances(), n));
		});
		
	}
     
    //count data population in groups
    private int[] getHistogram(List<Double> values, int numBars){
    	int[] histogram = new int[numBars];
    	DescriptiveStatistics stats = new DescriptiveStatistics();
    	values.forEach(v -> stats.addValue(v));
    	double min = stats.getMin();
    	double stepSize = (stats.getMax() - min) / numBars;
        for(int i = 0; i < values.size(); i++){
        	for (int j = 0; j < numBars; j++) {
        		if (values.get(i) <= min + (j * stepSize)) {
        			histogram[j] ++;
        			break;
        		}
        	}
        }
        return histogram;
    }

	@Override
	public String getTitle() {
		return "Search Rollout Statistics";
	}

}

package hasco.gui.statsplugin;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import hasco.core.HASCOSolutionCandidate;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.graphvisualizer.events.gui.Histogram;
import jaicore.graphvisualizer.plugin.ASimpleMVCPluginView;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TreeView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/**
 * 
 * @author fmohr
 *
 */
public class HASCOModelStatisticsPluginView extends ASimpleMVCPluginView<HASCOModelStatisticsPluginModel, HASCOModelStatisticsPluginController> {

	private final Histogram histogram;
	private final VBox root = new VBox();
	private final TreeView<FlowPane> treeView;
	private final int n = 100;
	private final HASCOModelStatisticsComponentSelector rootNode;

	public HASCOModelStatisticsPluginView(HASCOModelStatisticsPluginModel model) {
		super(model);
		rootNode = new HASCOModelStatisticsComponentSelector(this, model);
		treeView = new TreeView<>(rootNode);
		root.getChildren().add(treeView);
		histogram = new Histogram(n);
		histogram.setTitle("Performances observed on the filtered solutions");
		root.getChildren().add(histogram);
	}

	@Override
	public Node getNode() {
		return root;
	}

	@Override
	public void update() {
		rootNode.update();
		updateHistogram();
	}

	public void updateHistogram() {
		Collection<List<Pair<String, String>>> activeFilters = rootNode.getAllSelectionsOnPathToAnyLeaf();
		List<HASCOSolutionCandidate<Double>> activeSolutions = getModel().getAllSeenSolutionEventsUnordered().stream().map(s -> s.getSolutionCandidate()).filter(ci -> ci.getComponentInstance().matchesPathRestrictions(activeFilters))
				.collect(Collectors.toList());
		DescriptiveStatistics stats = new DescriptiveStatistics();
		activeSolutions.forEach(s -> stats.addValue(s.getScore()));
		Platform.runLater(() -> {
			histogram.update(getHistogram(stats, n));
		});
	}

	// count data population in groups
	private int[] getHistogram(DescriptiveStatistics stats, int numBars) {
		int[] histogram = new int[numBars];
		double[] values = stats.getValues();
		double min = stats.getMin();
		double stepSize = (stats.getMax() - min) / numBars;
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < numBars; j++) {
				if (values[i] <= min + (j * stepSize)) {
					histogram[j]++;
					break;
				}
			}
		}
		return histogram;
	}

	@Override
	public String getTitle() {
		return "HASCO Model Statistics";
	}

	@Override
	public void clear() {
		histogram.clear();
		rootNode.clear();
	}

}

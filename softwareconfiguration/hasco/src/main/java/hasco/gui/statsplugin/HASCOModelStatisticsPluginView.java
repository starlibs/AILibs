package hasco.gui.statsplugin;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.graphvisualizer.events.gui.Histogram;
import jaicore.graphvisualizer.plugin.ASimpleMVCPluginView;
import jaicore.graphvisualizer.plugin.solutionperformanceplotter.ScoredSolutionCandidateInfo;
import javafx.application.Platform;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

/**
 * 
 * @author fmohr
 *
 */
public class HASCOModelStatisticsPluginView extends ASimpleMVCPluginView<HASCOModelStatisticsPluginModel, HASCOModelStatisticsPluginController, VBox> {

	private final HASCOModelStatisticsComponentSelector rootNode; // the root of the TreeView shown at the top
	private final Histogram histogram; // the histogram shown on the bottom

	public HASCOModelStatisticsPluginView(HASCOModelStatisticsPluginModel model) {
		this(model, 100);
	}

	public HASCOModelStatisticsPluginView(HASCOModelStatisticsPluginModel model, int n) {
		super(model, new VBox());
		rootNode = new HASCOModelStatisticsComponentSelector(this, model);
		TreeView<HASCOModelStatisticsComponentSelector> treeView = new TreeView<>();
		treeView.setCellFactory((TreeView<HASCOModelStatisticsComponentSelector> tv) -> new HASCOModelStatisticsComponentCell(tv));
		treeView.setRoot(rootNode);
		getNode().getChildren().add(treeView);
		histogram = new Histogram(n);
		histogram.setTitle("Performances observed on the filtered solutions");
		getNode().getChildren().add(histogram);
	}

	@Override
	public void update() {
		rootNode.update();
		updateHistogram();
	}

	/**
	 * Updates the histogram at the bottom. This is called in both the update method of the general view as well as in the change listener of the combo boxes.
	 */
	public void updateHistogram() {
		Collection<List<Pair<String, String>>> activeFilters = rootNode.getAllSelectionsOnPathToAnyLeaf();
		List<ScoredSolutionCandidateInfo> activeSolutions = getModel().getAllSeenSolutionCandidateFoundInfosUnordered().stream()
				.filter(i -> getModel().deserializeComponentInstance(i.getSolutionCandidateRepresentation()).matchesPathRestrictions(activeFilters)).collect(Collectors.toList());
		DescriptiveStatistics stats = new DescriptiveStatistics();
		activeSolutions.forEach(s -> stats.addValue(getModel().parseScoreToDouble(s.getScore())));
		Platform.runLater(() -> {
			histogram.update(stats);
		});
	}

	@Override
	public String getTitle() {
		return "HASCO Model Statistics";
	}

	@Override
	public void clear() {
		rootNode.clear();
		histogram.clear();
	}
}

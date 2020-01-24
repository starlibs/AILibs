package ai.libs.hasco.gui.statsplugin;

import java.util.Arrays;
import java.util.Collection;

import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter.ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.search.gui.plugins.rollouthistograms.RolloutInfoAlgorithmEventPropertyComputer;

/**
 *
 * @author fmohr
 *
 *         The main class of this plugin. Add instances of this plugin to the visualization window.
 */
public class HASCOModelStatisticsPlugin extends ASimpleMVCPlugin<HASCOModelStatisticsPluginModel, HASCOModelStatisticsPluginView, HASCOModelStatisticsPluginController> {

	public HASCOModelStatisticsPlugin() {
		super();
	}

	@Override
	public Collection<AlgorithmEventPropertyComputer> getPropertyComputers() {
		return Arrays.asList(new RolloutInfoAlgorithmEventPropertyComputer(), new ScoredSolutionCandidateInfoAlgorithmEventPropertyComputer(new HASCOSolutionCandidateRepresenter()));
	}
}

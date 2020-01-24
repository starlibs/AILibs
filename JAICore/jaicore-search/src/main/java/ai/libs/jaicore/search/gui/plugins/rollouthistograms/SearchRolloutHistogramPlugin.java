package ai.libs.jaicore.search.gui.plugins.rollouthistograms;

import java.util.Arrays;
import java.util.Collection;

import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPlugin;

public class SearchRolloutHistogramPlugin extends ASimpleMVCPlugin<SearchRolloutHistogramPluginModel, SearchRolloutHistogramPluginView, SearchRolloutHistogramPluginController> {

	public SearchRolloutHistogramPlugin() {
		super();
	}

	@Override
	public Collection<AlgorithmEventPropertyComputer> getPropertyComputers() {
		return Arrays.asList(new RolloutInfoAlgorithmEventPropertyComputer());
	}
}

package ai.libs.jaicore.search.gui.plugins.rolloutboxplots;

import java.util.Arrays;
import java.util.Collection;

import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPlugin;
import ai.libs.jaicore.search.gui.plugins.rollouthistograms.RolloutInfoAlgorithmEventPropertyComputer;

public class SearchRolloutBoxplotPlugin extends ASimpleMVCPlugin<SearchRolloutBoxplotPluginModel, SearchRolloutBoxplotPluginView, SearchRolloutBoxplotPluginController> {

	public SearchRolloutBoxplotPlugin() {
		super();
	}

	@Override
	public Collection<AlgorithmEventPropertyComputer> getPropertyComputers() {
		return Arrays.asList(new RolloutInfoAlgorithmEventPropertyComputer());
	}
}

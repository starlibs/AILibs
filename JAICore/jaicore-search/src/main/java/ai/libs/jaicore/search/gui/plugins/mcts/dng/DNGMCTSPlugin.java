package ai.libs.jaicore.search.gui.plugins.mcts.dng;

import java.util.Arrays;
import java.util.Collection;

import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.search.gui.plugins.rollouthistograms.RolloutInfoAlgorithmEventPropertyComputer;

public class DNGMCTSPlugin extends ASimpleMVCPlugin<DNGMCTSPluginModel, DNGMCTSPluginView, DNGMCTSPluginController> {

	public DNGMCTSPlugin() {
		super();
	}

	@Override
	public Collection<AlgorithmEventPropertyComputer> getPropertyComputers() {
		return Arrays.asList(new RolloutInfoAlgorithmEventPropertyComputer(), new DNGEventPropertyComputer(new NodeInfoAlgorithmEventPropertyComputer()));
	}
}

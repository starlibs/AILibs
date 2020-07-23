package ai.libs.jaicore.search.gui.plugins.mcts.bradleyterry;

import java.util.Arrays;
import java.util.Collection;

import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.search.gui.plugins.rollouthistograms.RolloutInfoAlgorithmEventPropertyComputer;

public class BradleyTerryPlugin extends ASimpleMVCPlugin<BradleyTerryPluginModel, BradleyTerryPluginView, BradleyTerryPluginController> {

	public BradleyTerryPlugin() {
		this("Bradley Terry Plugin");
	}

	public BradleyTerryPlugin(final String title) {
		super(title);
	}

	@Override
	public Collection<AlgorithmEventPropertyComputer> getPropertyComputers() {
		return Arrays.asList(new RolloutInfoAlgorithmEventPropertyComputer(), new BradleyTerryEventPropertyComputer(new NodeInfoAlgorithmEventPropertyComputer()));
	}
}

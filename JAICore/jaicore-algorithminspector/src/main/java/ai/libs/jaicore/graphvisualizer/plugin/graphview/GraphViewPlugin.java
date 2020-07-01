package ai.libs.jaicore.graphvisualizer.plugin.graphview;

import java.util.Arrays;
import java.util.Collection;

import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoAlgorithmEventPropertyComputer;

public class GraphViewPlugin extends ASimpleMVCPlugin<GraphViewPluginModel, GraphViewPluginView, GraphViewPluginController> {

	@Override
	public void stop() {
		super.stop();
		this.getView().stop();
	}

	@Override
	public Collection<AlgorithmEventPropertyComputer> getPropertyComputers() {
		return Arrays.asList(new NodeInfoAlgorithmEventPropertyComputer());
	}
}

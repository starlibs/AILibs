package ai.libs.jaicore.graphvisualizer.plugin.nodeinfo;

import java.util.Arrays;
import java.util.Collection;

import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPlugin;

public class NodeInfoGUIPlugin extends ASimpleMVCPlugin<NodeInfoGUIPluginModel, NodeInfoGUIPluginView, NodeInfoGUIPluginController> {

	private final NodeInfoGenerator<?> infoGenerator;

	public NodeInfoGUIPlugin(final NodeInfoGenerator<?> infoGenerator) {
		super();
		this.infoGenerator = infoGenerator;
	}

	@Override
	public Collection<AlgorithmEventPropertyComputer> getPropertyComputers() {
		return Arrays.asList(new NodeDisplayInfoAlgorithmEventPropertyComputer<>(this.infoGenerator));
	}
}

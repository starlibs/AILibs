package ai.libs.jaicore.graphvisualizer.plugin.graphview;

import java.util.Arrays;
import java.util.Collection;

import ai.libs.jaicore.graphvisualizer.IColorMap;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPlugin;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoAlgorithmEventPropertyComputer;

public class GraphViewPlugin extends ASimpleMVCPlugin<GraphViewPluginModel, GraphViewPluginView, GraphViewPluginController> {

	public GraphViewPlugin() {
		this("Search Graph Viewer");
	}

	public GraphViewPlugin(final String title) {
		super(title);
	}

	@Override
	public void stop() {
		super.stop();
		this.getView().stop();
	}

	@Override
	public Collection<AlgorithmEventPropertyComputer> getPropertyComputers() {
		return Arrays.asList(new NodeInfoAlgorithmEventPropertyComputer());
	}

	public GraphViewPlugin withNodeColoringBasedOnProperty(final String propertyName, final IColorMap colorScheme, final double min, final double max) {
		this.getModel().setPropertyBasedNodeColoring(propertyName, colorScheme, min, max);
		return this;
	}

	public GraphViewPlugin withLabelsForNodeProperties() {
		this.getModel().setWithPropertyLabels(true);
		return this;
	}
}

package ai.libs.jaicore.graphvisualizer.plugin.controlbar;

import java.util.Arrays;
import java.util.Collection;

import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPlugin;

public class ControlBarGUIPlugin extends ASimpleMVCPlugin<ControlBarGUIPluginModel, ControlBarGUIPluginView, ControlBarGUIPluginController> {

	public ControlBarGUIPlugin() {
		this("Control Bar");
	}

	public ControlBarGUIPlugin(final String title) {
		super(title);
	}

	@Override
	public void stop() {
		this.getController().interrupt();
	}

	@Override
	public Collection<AlgorithmEventPropertyComputer> getPropertyComputers() {
		return Arrays.asList(); // no computers required
	}
}

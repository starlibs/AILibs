package ai.libs.jaicore.graphvisualizer.plugin.speedslider;

import java.util.Arrays;
import java.util.Collection;

import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPlugin;

public class SpeedSliderGUIPlugin extends ASimpleMVCPlugin<SpeedSliderGUIPluginModel, SpeedSliderGUIPluginView, SpeedSliderGUIPluginController> {

	public SpeedSliderGUIPlugin() {
		super();
	}

	@Override
	public void stop() {
		/* nothing to do here */
	}

	@Override
	public Collection<AlgorithmEventPropertyComputer> getPropertyComputers() {
		return Arrays.asList(); // no properties required
	}
}

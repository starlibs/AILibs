package ai.libs.jaicore.graphvisualizer.plugin.timeslider;

import java.util.Arrays;
import java.util.Collection;

import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPlugin;

public class TimeSliderGUIPlugin extends ASimpleMVCPlugin<TimeSliderGUIPluginModel, TimeSliderGUIPluginView, TimeSliderGUIPluginController>{

	public TimeSliderGUIPlugin() {
		super();
	}

	@Override
	public void stop() {
		this.getController().interrupt();
	}

	@Override
	public Collection<AlgorithmEventPropertyComputer> getPropertyComputers() {
		return Arrays.asList();
	}
}

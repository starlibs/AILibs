package ai.libs.jaicore.graphvisualizer.plugin;

import java.util.Collection;

import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;

public interface IComputedGUIPlugin extends IGUIPlugin {

	/**
	 * Gets the property computers that are necessary to run this plugin
	 *
	 * @return The {@link AlgorithmEventPropertyComputer} collection
	 */
	public Collection<AlgorithmEventPropertyComputer> getPropertyComputers();

}

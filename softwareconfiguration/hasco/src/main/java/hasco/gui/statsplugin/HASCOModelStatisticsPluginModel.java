package hasco.gui.statsplugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import hasco.events.HASCOSolutionEvent;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.UnparametrizedComponentInstance;
import jaicore.graphvisualizer.plugin.ASimpleMVCPluginModel;

/**
 * 
 * @author fmohr
 * 
 *         Holds all the information to supply the HASCOModelStatisticsPluginView with what it needs.
 */
public class HASCOModelStatisticsPluginModel extends ASimpleMVCPluginModel<HASCOModelStatisticsPluginView, HASCOModelStatisticsPluginController> {

	private final Map<UnparametrizedComponentInstance, List<HASCOSolutionEvent<Double>>> observedSolutionsGroupedModuloParameters = new HashMap<>();
	private final Map<String, Component> knownComponents = new HashMap<>();

	/**
	 * Informs the plugin about a new HASCOSolution. This solution will be considered in the combo boxes as well as in the histogram.
	 * 
	 * @param solutionEvent
	 */
	public final void addEntry(HASCOSolutionEvent<Double> solutionEvent) {
		ComponentInstance ci = solutionEvent.getSolutionCandidate().getComponentInstance();
		UnparametrizedComponentInstance uci = new UnparametrizedComponentInstance(ci);
		if (!observedSolutionsGroupedModuloParameters.containsKey(uci))
			observedSolutionsGroupedModuloParameters.put(uci, new ArrayList<>());
		observedSolutionsGroupedModuloParameters.get(uci).add(solutionEvent);
		ci.getContainedComponents().forEach(c -> {
			if (!knownComponents.containsKey(c.getName()))
				knownComponents.put(c.getName(), c);
		});
		getView().update();
	}

	/**
	 * Gets an (unordered) collection of the solutions received so far.
	 * 
	 * @return Collection of solutions.
	 */
	public Collection<HASCOSolutionEvent<Double>> getAllSeenSolutionEventsUnordered() {
		List<HASCOSolutionEvent<Double>> solutionEvents = new ArrayList<>();
		observedSolutionsGroupedModuloParameters.values().forEach(l -> solutionEvents.addAll(l));
		return solutionEvents;
	}

	/**
	 * Gets all solutions received so far grouped in a map in which the keys are unparametrized component instances.
	 * 
	 * @return Map with all solutions grouped by unparametrized component instances
	 */
	public Map<UnparametrizedComponentInstance, List<HASCOSolutionEvent<Double>>> getObservedSolutionsGroupedModuloParameters() {
		return observedSolutionsGroupedModuloParameters;
	}

	/**
	 * @return A map that assigns, for each known component, its name to the Component object.
	 */
	public Map<String, Component> getKnownComponents() {
		return knownComponents;
	}

	/**
	 * 
	 * @param composition
	 * @return
	 */
	public DescriptiveStatistics getPerformanceStatisticsForComposition(UnparametrizedComponentInstance composition) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		observedSolutionsGroupedModuloParameters.get(composition).forEach(e -> stats.addValue(e.getSolutionCandidate().getScore()));
		return stats;
	}
	
	/**
	 * Clears the model (and subsequently the view)
	 */
	@Override
	public void clear() {
		observedSolutionsGroupedModuloParameters.clear();
		knownComponents.clear();
		getView().clear();
	}
}

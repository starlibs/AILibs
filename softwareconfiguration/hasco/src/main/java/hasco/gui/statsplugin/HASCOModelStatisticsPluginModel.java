package hasco.gui.statsplugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import hasco.events.HASCOSolutionEvent;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.UnparametrizedComponentInstance;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.graphvisualizer.plugin.ASimpleMVCPluginModel;

/**
 * 
 * @author fmohr
 *
 * @param <N>
 *            The node type class.
 */
public class HASCOModelStatisticsPluginModel extends ASimpleMVCPluginModel<HASCOModelStatisticsPluginView, HASCOModelStatisticsPluginController> {

	private final Map<UnparametrizedComponentInstance, List<HASCOSolutionEvent<Double>>> observedSolutionsGroupedByModuloParameters = new HashMap<>();
	private final Map<String, Component> knownComponents = new HashMap<>();
	
	public final void addEntry(HASCOSolutionEvent<Double> solutionEvent) {
		ComponentInstance ci = solutionEvent.getSolutionCandidate().getComponentInstance();
		UnparametrizedComponentInstance uci = new UnparametrizedComponentInstance(ci);
		if (!observedSolutionsGroupedByModuloParameters.containsKey(uci))
			observedSolutionsGroupedByModuloParameters.put(uci, new ArrayList<>());
		observedSolutionsGroupedByModuloParameters.get(uci).add(solutionEvent);
		ci.getContainedComponents().forEach(c -> {
			if (!knownComponents.containsKey(c.getName()))
				knownComponents.put(c.getName(), c);
		});
		getView().update();
	}
	
	public Collection<HASCOSolutionEvent<Double>> getAllSeenSolutionEventsUnordered() {
		List<HASCOSolutionEvent<Double>> solutionEvents = new ArrayList<>();
		observedSolutionsGroupedByModuloParameters.values().forEach(l -> solutionEvents.addAll(l));
		return solutionEvents;
	}
	
	public Map<UnparametrizedComponentInstance, List<HASCOSolutionEvent<Double>>> getObservedSolutionsGroupedByModuloParameters() {
		return observedSolutionsGroupedByModuloParameters;
	}

	public Map<String, Component> getKnownComponents() {
		return knownComponents;
	}

	public DescriptiveStatistics getPerformanceStatisticsForComposition(UnparametrizedComponentInstance composition) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		observedSolutionsGroupedByModuloParameters.get(composition).forEach(e -> stats.addValue(e.getSolutionCandidate().getScore()));
		return stats;
	}

	public Map<UnparametrizedComponentInstance, DescriptiveStatistics> getPerformanceStatisticsPerComposition() {
		Map<UnparametrizedComponentInstance, DescriptiveStatistics> statsMap = new HashMap<>();
		for (UnparametrizedComponentInstance composition : observedSolutionsGroupedByModuloParameters.keySet()) {
			statsMap.put(composition, getPerformanceStatisticsForComposition(composition));
		}
		return statsMap;
	}

	public DescriptiveStatistics getEvaluationTimeStatisticsForComposition(UnparametrizedComponentInstance composition) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		observedSolutionsGroupedByModuloParameters.get(composition).forEach(e -> stats.addValue(e.getSolutionCandidate().getTimeToEvaluateCandidate()));
		return stats;
	}

	public Map<UnparametrizedComponentInstance, DescriptiveStatistics> getEvaluationTimeStatisticsPerComposition() {
		Map<UnparametrizedComponentInstance, DescriptiveStatistics> statsMap = new HashMap<>();
		for (UnparametrizedComponentInstance composition : observedSolutionsGroupedByModuloParameters.keySet()) {
			statsMap.put(composition, getPerformanceStatisticsForComposition(composition));
		}
		return statsMap;
	}
	
	public Collection<UnparametrizedComponentInstance> getSeenUnparametrizedComponentsUnderPath(List<Pair<String, String>> path) {
		if (path.isEmpty()) {
			return observedSolutionsGroupedByModuloParameters.keySet();
		}
		List<Pair<String, String>> copy = new ArrayList<>(path);
		Pair<String, String> lastEntry = copy.remove(copy.size() - 1);
		Collection<UnparametrizedComponentInstance> instancesUpToParent = getSeenUnparametrizedComponentsUnderPath(copy);
		List<String> pathOfRequiredInterfaces = path.stream().map(p -> p.getX()).collect(Collectors.toList());
		pathOfRequiredInterfaces.remove(0); // the first entry is always empty and, hence, can be ignored
		System.out.println(pathOfRequiredInterfaces);
		return instancesUpToParent.stream().filter(e -> e.getSubComposition(pathOfRequiredInterfaces).getComponentName().equals(lastEntry.getY())).collect(Collectors.toList());
	}
}

package hasco.gui.statsplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import hasco.events.HASCOSolutionEvent;
import hasco.model.UnparametrizedComponentInstance;
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
	
	public final void addEntry(HASCOSolutionEvent<Double> solutionEvent) {
		UnparametrizedComponentInstance comp = new UnparametrizedComponentInstance(solutionEvent.getSolutionCandidate().getComponentInstance());
		if (!observedSolutionsGroupedByModuloParameters.containsKey(comp))
			observedSolutionsGroupedByModuloParameters.put(comp, new ArrayList<>());
		observedSolutionsGroupedByModuloParameters.get(comp).add(solutionEvent);
		getView().update();
	}
	
	public Map<UnparametrizedComponentInstance, List<HASCOSolutionEvent<Double>>> getObservedSolutionsGroupedByModuloParameters() {
		return observedSolutionsGroupedByModuloParameters;
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
}

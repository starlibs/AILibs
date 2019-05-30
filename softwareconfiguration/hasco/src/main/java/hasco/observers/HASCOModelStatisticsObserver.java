package hasco.observers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.eventbus.Subscribe;

import hasco.events.HASCOSolutionEvent;
import hasco.model.UnparametrizedComponentInstance;

public class HASCOModelStatisticsObserver {
	
	private final Map<UnparametrizedComponentInstance, List<HASCOSolutionEvent<Double>>> observedSolutionsGroupedByModuloParameters = new HashMap<>();
	
	@Subscribe
	public void receiveSolutionEvent(HASCOSolutionEvent<Double> event) {
		UnparametrizedComponentInstance comp = new UnparametrizedComponentInstance(event.getSolutionCandidate().getComponentInstance());
		if (!observedSolutionsGroupedByModuloParameters.containsKey(comp))
			observedSolutionsGroupedByModuloParameters.put(comp, new ArrayList<>());
		observedSolutionsGroupedByModuloParameters.get(comp).add(event);
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

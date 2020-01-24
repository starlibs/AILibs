package ai.libs.hasco.observers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.eventbus.Subscribe;

import ai.libs.hasco.events.HASCOSolutionEvent;
import ai.libs.hasco.model.UnparametrizedComponentInstance;

public class HASCOModelStatisticsObserver {

	private final Map<UnparametrizedComponentInstance, List<HASCOSolutionEvent<Double>>> observedSolutionsGroupedByModuloParameters = new HashMap<>();

	@Subscribe
	public void receiveSolutionEvent(final HASCOSolutionEvent<Double> event) {
		UnparametrizedComponentInstance comp = new UnparametrizedComponentInstance(event.getSolutionCandidate().getComponentInstance());
		if (!this.observedSolutionsGroupedByModuloParameters.containsKey(comp)) {
			this.observedSolutionsGroupedByModuloParameters.put(comp, new ArrayList<>());
		}
		this.observedSolutionsGroupedByModuloParameters.get(comp).add(event);
	}

	public Map<UnparametrizedComponentInstance, List<HASCOSolutionEvent<Double>>> getObservedSolutionsGroupedByModuloParameters() {
		return this.observedSolutionsGroupedByModuloParameters;
	}

	public DescriptiveStatistics getPerformanceStatisticsForComposition(final UnparametrizedComponentInstance composition) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		this.observedSolutionsGroupedByModuloParameters.get(composition).forEach(e -> stats.addValue(e.getSolutionCandidate().getScore()));
		return stats;
	}

	public Map<UnparametrizedComponentInstance, DescriptiveStatistics> getPerformanceStatisticsPerComposition() {
		Map<UnparametrizedComponentInstance, DescriptiveStatistics> statsMap = new HashMap<>();
		for (UnparametrizedComponentInstance composition : this.observedSolutionsGroupedByModuloParameters.keySet()) {
			statsMap.put(composition, this.getPerformanceStatisticsForComposition(composition));
		}
		return statsMap;
	}

	public DescriptiveStatistics getEvaluationTimeStatisticsForComposition(final UnparametrizedComponentInstance composition) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		this.observedSolutionsGroupedByModuloParameters.get(composition).forEach(e -> stats.addValue(e.getSolutionCandidate().getTimeToEvaluateCandidate()));
		return stats;
	}

	public Map<UnparametrizedComponentInstance, DescriptiveStatistics> getEvaluationTimeStatisticsPerComposition() {
		return this.getPerformanceStatisticsPerComposition();
	}
}

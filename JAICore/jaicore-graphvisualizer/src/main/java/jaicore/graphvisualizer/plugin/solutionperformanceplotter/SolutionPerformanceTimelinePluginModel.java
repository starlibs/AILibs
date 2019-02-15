package jaicore.graphvisualizer.plugin.solutionperformanceplotter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import jaicore.basic.algorithm.events.ScoredSolutionCandidateFoundEvent;
import jaicore.graphvisualizer.plugin.ASimpleMVCPluginModel;

public class SolutionPerformanceTimelinePluginModel extends ASimpleMVCPluginModel<SolutionPerformanceTimelinePluginView, SolutionPerformanceTimelinePluginController> {

	private final List<Pair<Integer, Double>> timedPerformances = new ArrayList<>();
	private long timestampOfFirstEvent = -1;

	public final void addEntry(ScoredSolutionCandidateFoundEvent<?, ? extends Number> solutionEvent) {
		int offset = 0;
		if (timestampOfFirstEvent == -1) {
			timestampOfFirstEvent = solutionEvent.getTimestamp();
		} else {
			offset = (int) (solutionEvent.getTimestamp() - timestampOfFirstEvent);
		}
		timedPerformances.add(new Pair<>(offset, (Double) solutionEvent.getScore()));
		getView().update();
	}

	public long getTimestampOfFirstEvent() {
		return timestampOfFirstEvent;
	}

	public List<Pair<Integer, Double>> getTimedPerformances() {
		return timedPerformances;
	}

	public void clear() {
		timedPerformances.clear();
		timestampOfFirstEvent = -1;
		getView().clear();
	}
}

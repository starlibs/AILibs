package ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginModel;

public class SolutionPerformanceTimelinePluginModel extends ASimpleMVCPluginModel<SolutionPerformanceTimelinePluginView, SolutionPerformanceTimelinePluginController> {

	private final List<Pair<Integer, Double>> timedPerformances = new ArrayList<>();
	private long timestampOfFirstEvent = -1;

	public final void addEntry(long timestampOfEvent, double score) {
		int offset = 0;
		if (timestampOfFirstEvent == -1) {
			timestampOfFirstEvent = timestampOfEvent;
		} else {
			offset = (int) (timestampOfEvent - timestampOfFirstEvent);
		}

		timedPerformances.add(new Pair<>(offset, score));
		getView().update();
	}

	public long getTimestampOfFirstEvent() {
		return timestampOfFirstEvent;
	}

	public List<Pair<Integer, Double>> getTimedPerformances() {
		return timedPerformances;
	}

	@Override
	public void clear() {
		timedPerformances.clear();
		timestampOfFirstEvent = -1;
		getView().clear();
	}
}

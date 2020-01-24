package ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginModel;

public class SolutionPerformanceTimelinePluginModel extends ASimpleMVCPluginModel<SolutionPerformanceTimelinePluginView, SolutionPerformanceTimelinePluginController> {

	private final List<Pair<Integer, Double>> timedPerformances = new ArrayList<>();
	private long timestampOfFirstEvent = -1;

	public final void addEntry(final long timestampOfEvent, final double score) {
		int offset = 0;
		if (this.timestampOfFirstEvent == -1) {
			this.timestampOfFirstEvent = timestampOfEvent;
		} else {
			offset = (int) (timestampOfEvent - this.timestampOfFirstEvent);
		}

		this.timedPerformances.add(new Pair<>(offset, score));
		this.getView().update();
	}

	public long getTimestampOfFirstEvent() {
		return this.timestampOfFirstEvent;
	}

	public List<Pair<Integer, Double>> getTimedPerformances() {
		return this.timedPerformances;
	}

	@Override
	public void clear() {
		this.timedPerformances.clear();
		this.timestampOfFirstEvent = -1;
		this.getView().clear();
	}
}

package ai.libs.mlplan.gui.outofsampleplots;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.ml.classification.IClassifier;

import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginModel;

/**
 *
 * @author fmohr
 *
 */
public class OutOfSampleErrorPlotPluginModel extends ASimpleMVCPluginModel<OutOfSampleErrorPlotPluginView, OutOfSampleErrorPlotPluginController> {

	private final List<Integer> timestamps = new ArrayList<>();
	private final List<IClassifier> classifiers = new ArrayList<>();
	private final List<List<Double>> performances = new ArrayList<>();
	private long timestampOfFirstEvent = -1;

	public final void addEntry(final long timestamp, final IClassifier classifier, final List<Double> performances) {
		int offset = 0;
		if (this.timestampOfFirstEvent == -1) {
			this.timestampOfFirstEvent = timestamp;
		} else {
			offset = (int) (timestamp - this.timestampOfFirstEvent);
		}
		this.timestamps.add(offset);
		this.classifiers.add(classifier);
		this.performances.add(performances);
		this.getView().update();
	}

	public long getTimestampOfFirstEvent() {
		return this.timestampOfFirstEvent;
	}

	@Override
	public void clear() {
		this.timestamps.clear();
		this.classifiers.clear();
		this.performances.clear();
		this.timestampOfFirstEvent = -1;
		this.getView().clear();
	}

	public List<Integer> getTimestamps() {
		return this.timestamps;
	}

	public List<IClassifier> getClassifiers() {
		return this.classifiers;
	}

	public List<List<Double>> getPerformances() {
		return this.performances;
	}

	public void setTimestampOfFirstEvent(final long timestampOfFirstEvent) {
		this.timestampOfFirstEvent = timestampOfFirstEvent;
	}
}

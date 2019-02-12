package de.upb.crc901.mlplan.gui.outofsampleplots;

import java.util.ArrayList;
import java.util.List;

import jaicore.graphvisualizer.plugin.ASimpleMVCPluginModel;
import weka.classifiers.Classifier;

/**
 * 
 * @author fmohr
 *
 */
public class OutOfSampleErrorPlotPluginModel extends ASimpleMVCPluginModel<OutOfSampleErrorPlotPluginView, OutOfSampleErrorPlotPluginController> {

	private final List<Integer> timestamps = new ArrayList<>();
	private final List<Classifier> classifiers = new ArrayList<>();
	private final List<List<Double>> performances = new ArrayList<>();
	private long timestampOfFirstEvent = -1;
	
	public final void addEntry(long timestamp, Classifier classifier, List<Double> performances) {
		int offset = 0;
		if (timestampOfFirstEvent == -1) {
			timestampOfFirstEvent = timestamp;
		}
		else
			offset = (int)(timestamp - timestampOfFirstEvent);
		this.timestamps.add(offset);
		this.classifiers.add(classifier);
		this.performances.add(performances);
		getView().update();
	}

	public long getTimestampOfFirstEvent() {
		return timestampOfFirstEvent;
	}

	public void clear() {
		timestamps.clear();
		classifiers.clear();
		performances.clear();
		timestampOfFirstEvent = -1;
		getView().clear();
	}

	public List<Integer> getTimestamps() {
		return timestamps;
	}

	public List<Classifier> getClassifiers() {
		return classifiers;
	}

	public List<List<Double>> getPerformances() {
		return performances;
	}

	public void setTimestampOfFirstEvent(long timestampOfFirstEvent) {
		this.timestampOfFirstEvent = timestampOfFirstEvent;
	}
}

package de.upb.crc901.mlplan.gui.outofsampleplots;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.graphvisualizer.plugin.ASimpleMVCPluginView;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;

/**
 * 
 * @author fmohr
 *
 */
public class OutOfSampleErrorPlotPluginView extends ASimpleMVCPluginView<OutOfSampleErrorPlotPluginModel, OutOfSampleErrorPlotPluginController, LineChart<Number, Number>> {

	private Logger logger = LoggerFactory.getLogger(OutOfSampleErrorPlotPluginView.class);
	private final Series<Number,Number> believedErrorSeries;
	private final Series<Number,Number> outOfSampleErrorSeries;
	private int nextIndexToDisplay = 0;

	public OutOfSampleErrorPlotPluginView(OutOfSampleErrorPlotPluginModel model) {
		super(model, new LineChart<>(new NumberAxis(), new NumberAxis()));
		getNode().getXAxis().setLabel("elapsed time (s)");
		getNode().setTitle(getTitle());
		believedErrorSeries = new Series<>();
		believedErrorSeries.setName("Believed (internal) Error");
		outOfSampleErrorSeries = new Series<>();
		outOfSampleErrorSeries.setName("Out-of-Sample Error");
		getNode().getData().add(believedErrorSeries);
		getNode().getData().add(outOfSampleErrorSeries);
	}

	@Override
	public void update() {
		
		/* compute data to add */
		List<Integer> observedTimestamps = getModel().getTimestamps();
		List<List<Double>> performances = getModel().getPerformances();
		List<Data<Number, Number>> believedErrors = new ArrayList<>();
		List<Data<Number, Number>> outOfSampleErrors = new ArrayList<>();
		for (; nextIndexToDisplay < observedTimestamps.size(); nextIndexToDisplay++) {
			int timestamp = observedTimestamps.get(nextIndexToDisplay) / 100;
			believedErrors.add(new Data<>(timestamp, performances.get(nextIndexToDisplay).get(0)));
			outOfSampleErrors.add(new Data<>(timestamp, performances.get(nextIndexToDisplay).get(1)));
		}
		logger.info("Adding {} values to chart.", believedErrors.size());
		Platform.runLater(() -> {
			believedErrorSeries.getData().addAll(believedErrors);
			outOfSampleErrorSeries.getData().addAll(outOfSampleErrors);
		});

	}

	@Override
	public String getTitle() {
		return "Out-of-Sample Error Timeline";
	}
	
	public void clear() {
		nextIndexToDisplay = 0;
		believedErrorSeries.getData().clear();
		outOfSampleErrorSeries.getData().clear();
	}

	public int getNextIndexToDisplay() {
		return nextIndexToDisplay;
	}	
}

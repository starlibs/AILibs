package jaicore.graphvisualizer.plugin.solutionperformanceplotter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;
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
public class SolutionPerformanceTimelinePluginView extends ASimpleMVCPluginView<SolutionPerformanceTimelinePluginModel, SolutionPerformanceTimelinePluginController, LineChart<Number, Number>> {

	private Logger logger = LoggerFactory.getLogger(SolutionPerformanceTimelinePluginView.class);

	private final Series<Number, Number> performanceSeries;
	private int nextIndexToDisplay = 0;

	public SolutionPerformanceTimelinePluginView(SolutionPerformanceTimelinePluginModel model) {
		super(model, new LineChart<>(new NumberAxis(), new NumberAxis()));

		// defining the axes
		getNode().getXAxis().setLabel("elapsed time (s)");

		// creating the chart
		getNode().setTitle(getTitle());
		// defining a series
		performanceSeries = new Series<>();
		getNode().getData().add(performanceSeries);
	}

	@Override
	public void update() {

		/* compute data to add */
		List<Pair<Integer, Double>> events = getModel().getTimedPerformances();
		List<Data<Number, Number>> values = new ArrayList<>();
		for (; nextIndexToDisplay < events.size(); nextIndexToDisplay++) {
			Pair<Integer, Double> entry = events.get(nextIndexToDisplay);
			values.add(new Data<>(entry.getKey() / 1000, entry.getValue()));
		}
		logger.info("Adding {} values to chart.", values.size());
		Platform.runLater(() -> {
			performanceSeries.getData().addAll(values);
		});

	}

	@Override
	public String getTitle() {
		return "Solution Performance Timeline";
	}

	public void clear() {
		nextIndexToDisplay = 0;
		performanceSeries.getData().clear();
	}

	public int getNextIndexToDisplay() {
		return nextIndexToDisplay;
	}
}

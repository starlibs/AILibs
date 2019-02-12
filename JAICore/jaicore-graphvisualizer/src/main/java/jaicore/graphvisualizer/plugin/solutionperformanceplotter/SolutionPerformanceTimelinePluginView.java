package jaicore.graphvisualizer.plugin.solutionperformanceplotter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.graphvisualizer.plugin.ASimpleMVCPluginView;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;

/**
 * 
 * @author fmohr
 *
 */
public class SolutionPerformanceTimelinePluginView extends ASimpleMVCPluginView<SolutionPerformanceTimelinePluginModel, SolutionPerformanceTimelinePluginController> {

	private Logger logger = LoggerFactory.getLogger(SolutionPerformanceTimelinePluginView.class);
	private final LineChart<Number, Number> lineChart;
	private final Series<Number,Number> performanceSeries;
	private int nextIndexToDisplay = 0;

	public SolutionPerformanceTimelinePluginView(SolutionPerformanceTimelinePluginModel model) {
		super(model);

		// defining the axes
		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel("elapsed time (s)");

		// creating the chart
		lineChart = new LineChart<Number, Number>(xAxis, yAxis);
		lineChart.setTitle("Solution performances over time");
		
		// defining a series
		performanceSeries = new Series<>();
		lineChart.getData().add(performanceSeries);
	}

	@Override
	public Node getNode() {
		return lineChart;
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

package ai.libs.jaicore.graphvisualizer.plugin.solutionperformanceplotter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.graphvisualizer.plugin.ASimpleMVCPluginView;
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

	public SolutionPerformanceTimelinePluginView(final SolutionPerformanceTimelinePluginModel model) {
		super(model, new LineChart<>(new NumberAxis(), new NumberAxis()));

		// defining the axes
		this.getNode().getXAxis().setLabel("elapsed time (s)");

		// creating the chart
		this.getNode().setTitle(this.getTitle());
		// defining a series
		this.performanceSeries = new Series<>();
		this.getNode().getData().add(this.performanceSeries);
	}

	@Override
	public void update() {

		/* compute data to add */
		List<Pair<Integer, Double>> events = this.getModel().getTimedPerformances();
		List<Data<Number, Number>> values = new ArrayList<>();
		for (; this.nextIndexToDisplay < events.size(); this.nextIndexToDisplay++) {
			Pair<Integer, Double> entry = events.get(this.nextIndexToDisplay);
			values.add(new Data<>(entry.getKey() / 1000.0, entry.getValue()));
		}
		this.logger.info("Adding {} values to chart.", values.size());
		Platform.runLater(() -> {
			this.performanceSeries.getData().addAll(values);
		});

	}

	@Override
	public String getTitle() {
		return "Solution Performance Timeline";
	}

	@Override
	public void clear() {
		this.nextIndexToDisplay = 0;
		this.performanceSeries.getData().clear();
	}

	public int getNextIndexToDisplay() {
		return this.nextIndexToDisplay;
	}
}

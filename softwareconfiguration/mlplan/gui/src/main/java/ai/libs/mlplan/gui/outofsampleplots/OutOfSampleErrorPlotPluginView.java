package ai.libs.mlplan.gui.outofsampleplots;

import java.util.ArrayList;
import java.util.List;

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
public class OutOfSampleErrorPlotPluginView extends ASimpleMVCPluginView<OutOfSampleErrorPlotPluginModel, OutOfSampleErrorPlotPluginController, LineChart<Number, Number>> {

	private Logger logger = LoggerFactory.getLogger(OutOfSampleErrorPlotPluginView.class);
	private final Series<Number, Number> believedErrorSeries;
	private final Series<Number, Number> outOfSampleErrorSeries;
	private int nextIndexToDisplay = 0;

	public OutOfSampleErrorPlotPluginView(final OutOfSampleErrorPlotPluginModel model) {
		super(model, new LineChart<>(new NumberAxis(), new NumberAxis()));
		this.getNode().getXAxis().setLabel("elapsed time (s)");
		this.believedErrorSeries = new Series<>();
		this.believedErrorSeries.setName("Believed (internal) Error");
		this.outOfSampleErrorSeries = new Series<>();
		this.outOfSampleErrorSeries.setName("Out-of-Sample Error");
		this.getNode().getData().add(this.believedErrorSeries);
		this.getNode().getData().add(this.outOfSampleErrorSeries);
	}

	@Override
	public void update() {

		/* compute data to add */
		List<Integer> observedTimestamps = this.getModel().getTimestamps();
		List<List<Double>> performances = this.getModel().getPerformances();
		List<Data<Number, Number>> believedErrors = new ArrayList<>();
		List<Data<Number, Number>> outOfSampleErrors = new ArrayList<>();
		for (; this.nextIndexToDisplay < observedTimestamps.size(); this.nextIndexToDisplay++) {
			int timestamp = observedTimestamps.get(this.nextIndexToDisplay) / 100;
			believedErrors.add(new Data<>(timestamp, performances.get(this.nextIndexToDisplay).get(0)));
			outOfSampleErrors.add(new Data<>(timestamp, performances.get(this.nextIndexToDisplay).get(1)));
		}
		this.logger.info("Adding {} values to chart.", believedErrors.size());
		Platform.runLater(() -> {
			this.believedErrorSeries.getData().addAll(believedErrors);
			this.outOfSampleErrorSeries.getData().addAll(outOfSampleErrors);
		});

	}

	@Override
	public void clear() {
		this.nextIndexToDisplay = 0;
		this.believedErrorSeries.getData().clear();
		this.outOfSampleErrorSeries.getData().clear();
	}

	public int getNextIndexToDisplay() {
		return this.nextIndexToDisplay;
	}
}

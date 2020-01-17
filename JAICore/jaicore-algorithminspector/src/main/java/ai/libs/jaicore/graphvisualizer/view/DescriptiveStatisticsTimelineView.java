package ai.libs.jaicore.graphvisualizer.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

/**
 *
 * @author fmohr
 *
 */
public class DescriptiveStatisticsTimelineView extends LineChart<Number, Number> {

	private Logger logger = LoggerFactory.getLogger(DescriptiveStatisticsTimelineView.class);

	private final DescriptiveStatistics stats = new DescriptiveStatistics();
	private final ObservableList<Double> scores;
	private final Series<Number, Number> performanceSeries;


	public DescriptiveStatisticsTimelineView(final ObservableList<Double> scores) {
		super(new NumberAxis(), new NumberAxis());

		// defining the axes
		this.getXAxis().setLabel("elapsed time (s)");

		// defining a series
		this.scores = scores;
		this.performanceSeries = new Series<>();
		this.getData().add(this.performanceSeries);
		this.update();
		scores.addListener(new ListChangeListener<Double>() {

			@Override
			public void onChanged(final Change<? extends Double> c) {
				DescriptiveStatisticsTimelineView.this.update();
			}
		});
	}

	public void clear() {
		this.performanceSeries.getData().clear();
		this.scores.clear();
	}

	public void update() {
		if (this.scores.isEmpty()) {
			this.performanceSeries.getData().clear();
		}
		else {
			int m = this.scores.size();
			int n = this.performanceSeries.getData().size();
			List<Data<Number, Number>> values = new ArrayList<>();
			for (int i = n; i < m; i++) {
				this.stats.addValue(this.scores.get(i));
				values.add(new Data<>((i + 1), this.stats.getMean()));
			}
			this.logger.info("Adding {} values to chart.", values.size());
			Platform.runLater(() -> this.performanceSeries.getData().addAll(values));
		}

	}
}

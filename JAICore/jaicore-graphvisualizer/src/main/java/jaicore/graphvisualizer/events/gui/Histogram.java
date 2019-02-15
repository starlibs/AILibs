package jaicore.graphvisualizer.events.gui;

import java.util.ArrayList;
import java.util.List;

import jaicore.graphvisualizer.IntegerAxisFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class Histogram extends BarChart<String, Number>{
	private final XYChart.Series<String, Number> series = new XYChart.Series<>();
	private final ObservableList<Data<String, Number>> histogram;
	private int max;
	
	public Histogram(int n) {
		super(new CategoryAxis(), new NumberAxis());
		this.getData().add(series);
		List<Data<String,Number>> values = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			values.add(new Data<>("" + i, 0));
		}
		histogram = FXCollections.observableList(values);
		series.setData(histogram);
		((NumberAxis)getYAxis()).setMinorTickVisible(false); // only show integers
		
		/* reasonable layout */
		this.setAnimated(false);
		this.setLegendVisible(false);
		((NumberAxis) getYAxis()).setTickUnit(1);
		((NumberAxis) getYAxis()).setTickLabelFormatter(new IntegerAxisFormatter());
		((NumberAxis) getYAxis()).setMinorTickCount(0);
	}
	
	public void update(int[] values) {
		List<Data<String,Number>> transformedValues = new ArrayList<>();
		for (int i = 0; i < values.length; i++) {
			if (values[i] > max) {
				max = values[i];
			}
			transformedValues.add(new Data<>("" + i, values[i]));
        }
		this.histogram.setAll(transformedValues);
	}
	
	public void clear() {
		max = 0;
		this.histogram.clear();
	}
}

package jaicore.graphvisualizer.events.gui;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class Histogram extends BarChart<String, Number>{
	private final XYChart.Series<String, Number> series = new XYChart.Series<>();
	private final ObservableList<Data<String, Number>> histogram;
	private String name = "Histogram";
	public Histogram(int n) {
		super(new CategoryAxis(), new NumberAxis());
		series.setName(name);
		this.setAnimated(false);
		this.getData().add(series);
		List<Data<String,Number>> values = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			values.add(new Data("" + i, 0));
		}
		histogram = FXCollections.observableList(values);
		series.setData(histogram);
	}
	
	public void update(int[] values) {
		Data<String,Number>[] transformedValues = new Data[values.length];
		for (int i = 0; i < values.length; i++) {
			transformedValues[i] = new Data<>("" + i, values[i]);
        }
		this.histogram.setAll(transformedValues);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		series.setName(name);
	}

}

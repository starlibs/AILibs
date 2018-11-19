package jaicore.graphvisualizer.events.misc;

import javafx.scene.chart.XYChart;

/**
 * A event which is used to add data to the xyGraphVisualizer
 *
 */
public class XYEvent {
	private XYChart.Data data;

	public XYEvent(XYChart.Data data) {
		this.data = data;
	}

	public XYChart.Data getData() {
		return data;
	}
}

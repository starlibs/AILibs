package jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.util.ArrayList;
import java.util.List;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.Styler.LegendPosition;
import jaicore.search.algorithms.standard.uncertainty.UncertaintyFMeasure;

public class ParetoFrontVisualizer {

	final XYChart chart;
	final SwingWrapper<XYChart> vis;
	final List<UncertaintyFMeasure> points;
	
	public ParetoFrontVisualizer () {
		chart = new XYChartBuilder()
				.width(600)
				.height(500)
				.title("Paretofront Visualizer")
				.theme(ChartTheme.Matlab)
				.xAxisTitle("Uncertainty")
				.yAxisTitle("F Value")
				.build();
		
		chart.getStyler().setYAxisMin(0.0d);
		chart.getStyler().setXAxisMin(0.0d);
		chart.getStyler().setYAxisMax(1.0d);
		chart.getStyler().setXAxisMax(1.0d);
		chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
		chart.getStyler().setLegendPosition(LegendPosition.OutsideS);
		chart.getStyler().setMarkerSize(4);
		chart.addSeries("Paretofront Candidates", new double[] {1}, new double[] {1});
		
		vis = new SwingWrapper<>(chart);
		
		points = new ArrayList<>();
	}
	
	public void show () {
		vis.displayChart();
	}
	
	public void update (UncertaintyFMeasure point) {
		points.add(point);
		
		double[] u = new double[points.size()];
		double[] f = new double[points.size()];
		for (int i = 0; i < points.size(); i++) {
			u[i] = points.get(i).getUncertainty();
			f[i] = points.get(i).getfValue();
		}
		
		javax.swing.SwingUtilities.invokeLater(() -> {
			chart.updateXYSeries("Paretofront Candidates", u, f, null);
			vis.repaintChart();
		});
	}
	
}

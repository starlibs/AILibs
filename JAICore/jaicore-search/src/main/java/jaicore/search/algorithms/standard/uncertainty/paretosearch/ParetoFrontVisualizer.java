package jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.util.ArrayList;
import java.util.List;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.Styler.LegendPosition;

public class ParetoFrontVisualizer {

	private final XYChart chart;
	private final SwingWrapper<XYChart> vis;
	private final List<Double> fValues;
	private final List<Double> uncertainties;

	public ParetoFrontVisualizer () {
		this.chart = new XYChartBuilder()
				.width(600)
				.height(500)
				.title("Paretofront Visualizer")
				.theme(ChartTheme.Matlab)
				.xAxisTitle("Uncertainty")
				.yAxisTitle("F Value")
				.build();

		this.chart.getStyler().setYAxisMin(0.0d);
		this.chart.getStyler().setXAxisMin(0.0d);
		this.chart.getStyler().setYAxisMax(1.0d);
		this.chart.getStyler().setXAxisMax(1.0d);
		this.chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
		this.chart.getStyler().setLegendPosition(LegendPosition.OutsideS);
		this.chart.getStyler().setMarkerSize(4);
		this.chart.addSeries("Paretofront Candidates", new double[] {1}, new double[] {1});

		this.vis = new SwingWrapper<>(this.chart);

		this.fValues = new ArrayList<>();
		this.uncertainties = new ArrayList<>();
	}

	public void show () {
		this.vis.displayChart();
	}

	public void update (final double fValue, final double uncertainty) {
		this.fValues.add(fValue);
		this.uncertainties.add(uncertainty);

		if (this.fValues.size() == this.uncertainties.size()) {
			double[] f = new double[this.fValues.size()];
			double[] u = new double[this.uncertainties.size()];
			for (int i = 0; i < this.fValues.size(); i++) {
				f[i] = this.fValues.get(i);
				u[i] = this.uncertainties.get(i);
			}
			javax.swing.SwingUtilities.invokeLater(() -> {
				this.chart.updateXYSeries("Paretofront Candidates", u, f, null);
				this.vis.repaintChart();
			});
		} else {
			System.out.println("ERROR: Unqueal value amounts");
		}
	}

}

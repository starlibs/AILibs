package ai.libs.jaicore.ml.classification.singlelabel.timeseries.filter;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.TimeSeriesDataset2;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.util.HistogramBuilder;

public class HistogramBuilderTest {

	private double[] timeseries1;
	private double[] timeseries2;
	private TimeSeriesDataset2 dataset;

	@BeforeEach
	public void setup() {
		this.timeseries1 = new double[] { 1, 1, 1, 1, 1, 1, 1, 1 };
		this.timeseries2 = new double[] { 1, 2, 4, 3, 5, 2, 4, 3 };
		double[][] matrix = new double[3][8];
		matrix[0] = this.timeseries1;
		matrix[1] = this.timeseries2;

		ArrayList<double[][]> futureDataSet = new ArrayList<>();
		futureDataSet.add(matrix);
		this.dataset = new TimeSeriesDataset2(futureDataSet, null, null);
	}

	@Test
	public void test() {
		SFA testSFA = new SFA(new double[] { 1, 2, 3 }, 4);
		SlidingWindowBuilder builder = new SlidingWindowBuilder();
		builder.setDefaultWindowSize(3);
		for (double[][] matrix : this.dataset.getValueMatrices()) {
			for (double[] instance : matrix) {
				TimeSeriesDataset2 tmp = testSFA.fitTransform((builder.specialFitTransform(instance)));
				HistogramBuilder histoBuilder = new HistogramBuilder();
				Map<Integer, Integer> histo = histoBuilder.histogramForInstance(tmp);
			}
		}

		TimeSeriesDataset2 output = testSFA.fitTransform(this.dataset);
		assertEquals(2, output.getValues(0)[0][0], 1.0E-5);
	}
}

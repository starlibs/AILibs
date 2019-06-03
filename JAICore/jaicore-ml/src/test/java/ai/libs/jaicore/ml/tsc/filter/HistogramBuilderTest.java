package ai.libs.jaicore.ml.tsc.filter;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ai.libs.jaicore.ml.tsc.HistogramBuilder;
import ai.libs.jaicore.ml.tsc.dataset.TimeSeriesDataset;
import ai.libs.jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

@RunWith(JUnit4.class)
public class HistogramBuilderTest {

	double[] timeseries1;
	double[] timeseries2;
	TimeSeriesDataset dataset;

	@Before
	public void setup() throws Exception {
		this.timeseries1 = new double[] { 1, 1, 1, 1, 1, 1, 1, 1 };
		this.timeseries2 = new double[] { 1, 2, 4, 3, 5, 2, 4, 3 };
		double[][] matrix = new double[3][8];
		matrix[0] = this.timeseries1;
		matrix[1] = this.timeseries2;

		ArrayList<double[][]> futureDataSet = new ArrayList<double[][]>();
		futureDataSet.add(matrix);
		this.dataset = new TimeSeriesDataset(futureDataSet, null, null);
	}

	@Test
	public void test() {
		SFA testSFA = new SFA(new double[] { 1, 2, 3 }, 4);
		try {
			SlidingWindowBuilder builder = new SlidingWindowBuilder();
			builder.setDefaultWindowSize(3);
			for (double[][] matrix : this.dataset.getValueMatrices()) {
				for (double[] instance : matrix) {
					TimeSeriesDataset tmp = testSFA.fitTransform((builder.specialFitTransform(instance)));
					HistogramBuilder histoBuilder = new HistogramBuilder();
					HashMap<Integer, Integer> histo = histoBuilder.histogramForInstance(tmp);
					System.out.println(histo.toString());
					System.out.println("--------------------------------------------");
				}
			}

			TimeSeriesDataset output = testSFA.fitTransform(this.dataset);
			assertEquals(2, output.getValues(0)[0][0], 1.0E-5);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoneFittedFilterExeception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

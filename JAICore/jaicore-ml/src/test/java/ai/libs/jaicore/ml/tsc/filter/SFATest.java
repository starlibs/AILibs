package ai.libs.jaicore.ml.tsc.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ai.libs.jaicore.ml.tsc.dataset.TimeSeriesDataset;

@RunWith(JUnit4.class)
public class SFATest {

	double[] timeseries1;
	double[] timeseries2;
	TimeSeriesDataset dataset;

	@Before
	public void setup() {
		this.timeseries2 = new double[] { 1, 2, 4, 3, 5, 2, 4, 3 };
		double[][] matrix = new double[1][8];
		matrix[0] = this.timeseries2;

		ArrayList<double[][]> futureDataSet = new ArrayList<>();
		futureDataSet.add(matrix);
		this.dataset = new TimeSeriesDataset(futureDataSet, null, null);
	}

	@Test
	public void test() {
		SFA testSFA = new SFA(new double[] { 1, 2, 3 }, 4);
		SlidingWindowBuilder builder = new SlidingWindowBuilder();
		builder.setDefaultWindowSize(3);
		for (double[][] matrix : this.dataset.getValueMatrices()) {
			for (double[] instance : matrix) {
				TimeSeriesDataset tmp = testSFA.fitTransform((builder.specialFitTransform(instance)));
				for (double[][] m : tmp.getValueMatrices()) {
					for (double[] i : m) {
						fail("This fail is just here to announce that this test does not really test anything at all. Insert a meaningful check. Output to prevent SQ to fire: " + i);
					}
				}
			}
		}

		TimeSeriesDataset output = testSFA.fitTransform(this.dataset);
		assertEquals(2, output.getValues(0)[0][0], 1.0E-5);
	}

}

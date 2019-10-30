package ai.libs.jaicore.ml.classification.singlelabel.timeseries.filter;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.TimeSeriesDataset2;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.filter.SlidingWindowBuilder;

@RunWith(JUnit4.class)
public class SlidingWindowBuilderTest {

	private TimeSeriesDataset2 dataset;

	@Before
	public void setUp() {

		double[] timeseries1 = new double[] { 1, 2, 1, 26, 1, 77 };
		double[] timeseries2 = new double[] { 50, 100, 2, 7, 6, 70 };

		double[][] matrix = new double[2][6];
		matrix[0] = timeseries1;

		double[][] matrix2 = new double[2][6];
		matrix2[0] = timeseries2;

		List<double[][]> futureDataSet = new ArrayList<>();
		futureDataSet.add(matrix);
		futureDataSet.add(matrix2);
		this.dataset = new TimeSeriesDataset2(futureDataSet, null, null);
	}

	@Test
	public void test() {
		SlidingWindowBuilder builder = new SlidingWindowBuilder();
		builder.setDefaultWindowSize(2);
		for (double[][] matrix : this.dataset.getValueMatrices()) {
			for (double[] instance : matrix) {
				TimeSeriesDataset2 tmp = builder.specialFitTransform(instance);
				for (double[][] newMatrix : tmp.getValueMatrices()) {
					for (double[] entry : newMatrix) {
						fail("This fail is just here to announce that this test does not really test anything at all. Insert a meaningful check.");
					}
				}
			}
		}

	}
}

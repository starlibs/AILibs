package ai.libs.jaicore.ml.classification.singlelabel.timeseries.filter;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.TimeSeriesDataset2;

public class zTransformTest {

	private double[] timeseries1, timeseries2;
	// private double[] timeseries2;
	private TimeSeriesDataset2 dataset;

	@BeforeEach
	public void setUp() throws Exception {

		this.timeseries1 = new double[] { 1, 2, 1, 26, 1, 77 };
		this.timeseries2 = new double[] { 50, 100, 2, 7, 6, 70 };

		double[][] matrix = new double[2][6];
		matrix[0] = this.timeseries1;

		double[][] matrix2 = new double[2][6];
		matrix2[0] = this.timeseries2;

		ArrayList<double[][]> futureDataSet = new ArrayList<double[][]>();
		futureDataSet.add(matrix);
		futureDataSet.add(matrix2);
		this.dataset = new TimeSeriesDataset2(futureDataSet, null, null);
	}

	@Test
	public void test() {
		ZTransformer test = new ZTransformer();
		test.setBasselCorrected(false);
		assertEquals(1, this.dataset.getValues(0)[0][0], 1.0E-5);
	}

}

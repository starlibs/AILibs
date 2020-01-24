package ai.libs.jaicore.ml.classification.singlelabel.timeseries.filter;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.TimeSeriesDataset2;


@RunWith(JUnit4.class)
public class zTransformTest {


	double[] timeseries1, timeseries2 ;
	//double[] timeseries2;
	TimeSeriesDataset2 dataset;

	@Before
	public void setUp() throws Exception {

		this.timeseries1 = new double [] {1,2,1,26,1,77};
		this.timeseries2 = new double [] {50,100,2,7,6,70};

		double[][] matrix = new double[2][6];
		matrix[0] = this.timeseries1;

		double[][] matrix2 = new double[2][6];
		matrix2[0] = this.timeseries2;

		ArrayList<double[][]> futureDataSet = new ArrayList<double[][]>();
		futureDataSet.add(matrix);
		futureDataSet.add(matrix2);
		this.dataset = new TimeSeriesDataset2(futureDataSet,null, null);
	}

	@Test
	public void test() {
		ZTransformer test = new ZTransformer();
		test.setBasselCorrected(false);
		for(double[][] matrix: this.dataset.getValueMatrices()) {
			for(double[] istance: matrix) {
				System.out.println(Arrays.toString(istance));
			}
			System.out.println("--------------------------------------------------");
		}
		TimeSeriesDataset2 tmp = test.fitTransform(this.dataset);
		for(double[][] matrix: tmp.getValueMatrices()) {
			for(double[] istance: matrix) {
				System.out.println(Arrays.toString(istance));
			}
			System.out.println("--------------------------------------------------");
		}

		for(double[][] matrix: tmp.getValueMatrices()) {
			for(double[] istance: matrix) {
				System.out.println(Arrays.stream(istance).average());
			}
			System.out.println("--------------------------------------------------");
		}


		assertEquals(1, this.dataset.getValues(0)[0][0],1.0E-5);
	}

}

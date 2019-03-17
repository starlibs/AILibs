package jaicore.ml.tsc.filter;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

import static org.junit.Assert.assertEquals;


@RunWith(JUnit4.class)
public class zTransformTest {

	
	double[] timeseries1, timeseries2 ;
	//double[] timeseries2;
	TimeSeriesDataset dataset;
	
	@Before
	public void setUp() throws Exception {
		
		timeseries1 = new double [] {1,2,1,26,1,77};
		timeseries2 = new double [] {50,100,2,7,6,70};
		
		double[][] matrix = new double[2][6];
		matrix[0] = timeseries1;
		
		double[][] matrix2 = new double[2][6];
		matrix2[0] = timeseries2;
		
		ArrayList<double[][]> futureDataSet = new ArrayList<double[][]>();
		futureDataSet.add(matrix);
		futureDataSet.add(matrix2);
		dataset = new TimeSeriesDataset(futureDataSet,null, null);
	}

	@Test
	public void test() {
		ZTransformer test = new ZTransformer();
		test.setBasselCorrected(false);
		try {
			for(double[][] matrix: dataset.getValueMatrices()) {
				for(double[] istance: matrix) {
					System.out.println(Arrays.toString(istance));
				}
				System.out.println("--------------------------------------------------");
			}
			TimeSeriesDataset tmp = test.fitTransform(dataset);
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
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoneFittedFilterExeception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(1, dataset.getValues(0)[0][0],1.0E-5);
	}

}

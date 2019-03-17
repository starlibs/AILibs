package jaicore.ml.tsc.filter;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;

import static org.junit.Assert.assertEquals;


@RunWith(JUnit4.class)
public class SlidingWindowBuilderTest {

	
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
		SlidingWindowBuilder builder = new SlidingWindowBuilder();
		builder.setDefaultWindowSize(2);
		for(double [][] matrix : dataset.getValueMatrices()) {
			for(double[] instance : matrix) {
				try {
					TimeSeriesDataset tmp = builder.specialFitTransform(instance);
					for(double[][] newMatrix : tmp.getValueMatrices()) {
						for(double [] entry : newMatrix) {
							System.out.println(Arrays.toString(entry));
						}
						System.out.println("------------------------------------------");
					}
					
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
		
	}
}

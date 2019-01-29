package jaicore.ml.tsc.filter;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;


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
		
		try {
			test.fitTransform(dataset);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoneFittedFilterExeception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertEquals(1007, dataset.getValues(0)[0][0],1.0E-5);
	}

}

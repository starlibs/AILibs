package jaicore.ml.tsc.filter;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;


import java.util.ArrayList;

import org.junit.Before;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

@RunWith(JUnit4.class)
public class SFATest {
	
	double[] timeseries1;
	TimeSeriesDataset dataset;
	
	@Before
	public void setup() throws Exception {
		timeseries1 = new double [] {1,1,1,1,1,1};
		double[][] matrix = new double[2][6];
		matrix[0] = timeseries1;
		
		ArrayList<double[][]> futureDataSet = new ArrayList<double[][]>();
		futureDataSet.add(matrix);
		dataset = new TimeSeriesDataset(futureDataSet,null, null);
	}

	@Test
	public void test() {
		SFA testSFA = new SFA(new double[]{1,2},4);
		try {
			TimeSeriesDataset output = testSFA.fitTransform(dataset);
			assertEquals(2,output.getValues(0)[0][0],1.0E-5);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoneFittedFilterExeception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

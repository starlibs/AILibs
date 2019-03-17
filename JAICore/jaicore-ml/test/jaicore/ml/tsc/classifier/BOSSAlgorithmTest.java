package jaicore.ml.tsc.classifier;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Rule;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

/**
 * @author Helen
 * DFT JUnit test 
 *
 */
@RunWith(JUnit4.class)
public class BOSSAlgorithmTest {
	double[] timeseries1;
	double[] timeseries2;
	
	TimeSeriesDataset dataset;
	
	@Before
	public void setup() {
		timeseries1 = new double [] {1,1,1,1,1,1,1,1};
		timeseries2 = new double[] {1,2,4,3,5,2,4,3};
		double[][] matrix = new double[3][8];
		matrix[0] = timeseries1;
		matrix[1] = timeseries2;
		
		ArrayList<double[][]> futureDataSet = new ArrayList<double[][]>();
		futureDataSet.add(matrix);
		dataset = new TimeSeriesDataset(futureDataSet,null, null);
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void testFit() throws IllegalArgumentException, NoneFittedFilterExeception {
		
		BOSSClassifier test2 = new BOSSClassifier(3, 3, 4,new double[]{1.0,2.0,3.0},false);
		try {
			test2.algorithm.setInput(dataset);
			test2.algorithm.setModel(test2);
			test2.algorithm.call();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlgorithmExecutionCanceledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

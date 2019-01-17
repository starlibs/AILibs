package jaicore.ml.tsc.filter;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;


import java.util.ArrayList;

import org.junit.Before;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

/**
 * @author Helen
 * DFT JUnit test 
 *
 */
@RunWith(JUnit4.class)
public class DFTTest {
	INDArray timeseries1;
	//INDArray timeseries2;
	TimeSeriesDataset dataset;
	
	@Before
	public void setup() {
		timeseries1 = Nd4j.ones(1,6);
		INDArray matrix = Nd4j.zeros(2,6);
		matrix.putRow(0, timeseries1);
		
		ArrayList<INDArray> futureDataSet = new ArrayList<INDArray>();
		futureDataSet.add(matrix);
		dataset = new TimeSeriesDataset(futureDataSet,null, null);
	}
	
	@Test
	public void testFit() {
		DFT testDFT = new DFT();
		testDFT.setNumberOfDisieredCoefficients(2);
		testDFT.fit(dataset);
		TimeSeriesDataset output = null;
		try {
			 output = (TimeSeriesDataset)testDFT.transform(dataset);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoneFittedFilterExeception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals(1,output.getValues(0).getRow(0).getDouble(0), 1.0E-5);
		assertEquals(0, output.getValues(0).getRow(0).getDouble(1), 1.0E-5);
		
	}

}

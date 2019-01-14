package jaicore.ml.tsc.filter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.core.dataset.TimeSeriesDataset;

/**
 * @author Helen
 * DFT JUnit test 
 *
 */
class DFTTest {
	INDArray timeseries1;
	INDArray timeseries2;
	TimeSeriesDataset dataset;
	
	@Before
	public void setup() {
		timeseries1 = Nd4j.create(new double[]{1,1,1,1,1,1});
		timeseries2 = Nd4j.zeros(1,6);
		ArrayList<INDArray> futureDataSet = new ArrayList<INDArray>();
		futureDataSet.add(Nd4j.vstack(timeseries1,timeseries2));
		dataset = new TimeSeriesDataset(futureDataSet,null, null);
	}
	@Test
	void testFit() {
		DFT testDFT = new DFT();
		testDFT.setNumberOfDisieredCoefficients(2);
		testDFT.fit(dataset);
		
		
		
	}

}

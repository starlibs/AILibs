package jaicore.ml.tsc.filter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;

class SFATest {
	
	double[] timeseries1;
	TimeSeriesDataset dataset;
	
	@BeforeEach
	void setUp() throws Exception {
		timeseries1 = new double [] {1,1,1,1,1,1};
		double[][] matrix = new double[2][6];
		matrix[0] = timeseries1;
		
		ArrayList<double[][]> futureDataSet = new ArrayList<double[][]>();
		futureDataSet.add(matrix);
		dataset = new TimeSeriesDataset(futureDataSet,null, null);
	}

	@Test
	void test() {
		SFA testSFA = new SFA(new double []{1,2,3},5);
	}

}

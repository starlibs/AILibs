package jaicore.ml.tsc.classifier;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.util.ClassMapper;
import jaicore.ml.tsc.util.SimplifiedTimeSeriesLoader;

public class BOSSClassifierRefTest {
	
	  /** Local path for the datasets arff files. */
    private static final String PATH = "./tsctestenv/data/UCR_TS_Archive_2015/";

    /** Path for pen digits dataset. */
    private static final String CAR_TRAIN = PATH + "Car/Car_TRAIN.arff";
    
    private static final String CAR_TEST = PATH + "Car/Car_TEST.arff";

    /** Dataset used for comparison tests. */
    private TimeSeriesDataset dataset;
    private TimeSeriesDataset dataset2;
		
	@Before
	public void setUp() throws TimeSeriesLoadingException {
	   // Load dataset.
	   File arffFile = new File(CAR_TRAIN);
	   Pair<TimeSeriesDataset, ClassMapper> trainPair = SimplifiedTimeSeriesLoader.loadArff(arffFile);
	   dataset = trainPair.getX();
	   
	   File arffFile2 = new File(CAR_TEST);
	   Pair<TimeSeriesDataset, ClassMapper> testPair2 = SimplifiedTimeSeriesLoader.loadArff(arffFile2);
	   dataset2 = testPair2.getX();
	}
	
	@Test
	public void testPerformance() {
		double[] alphabet = {2.0,3.0,1.0};
		BOSSClassifier test = new BOSSClassifier(30, 16, alphabet.length, alphabet, false);
		test.setTrainingData(dataset);
		//TODO look after isTrained method.
		//test
		
	}
}

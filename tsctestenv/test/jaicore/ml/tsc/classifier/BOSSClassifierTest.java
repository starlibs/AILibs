package jaicore.ml.tsc.classifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.exception.EvaluationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.util.ClassMapper;
import jaicore.ml.tsc.util.SimplifiedTimeSeriesLoader;

@RunWith(JUnit4.class)
public class BOSSClassifierTest {
	
	  /** Local path for the datasets arff files. */
	private static final String UNIVARIATE_PREFIX = "C:\\Users\\Helen\\Documents\\Uni Informatik\\PG\\AILibs\\tsctestenv\\data\\Downloads\\";

	private static final String CAR_TRAIN = UNIVARIATE_PREFIX + "Car\\Car\\Car_TRAIN.arff";
	private static final String CAR_TEST = UNIVARIATE_PREFIX + "Car\\Car\\Car_TEST.arff";

    /** Dataset used for comparison tests. */
    private TimeSeriesDataset dataset;
    private TimeSeriesDataset dataset2;
		
	@Before
	public void setUp() throws TimeSeriesLoadingException, FileNotFoundException, EvaluationException, TrainingException,
    PredictionException, IOException, ClassNotFoundException {
	   //File arffFile = new File(CAR_TRAIN);
		System.out.println("Hallo");
		String s = "C:\\Users\\Helen\\Documents\\Uni Informatik\\PG\\AILibs\\tsctestenv\\data\\Downloads\\Car\\Car\\Car_TRAIN.arff";
		System.out.println("Hallo");
		File arffFile = new File(s);
	   Pair<TimeSeriesDataset, ClassMapper> trainPair = SimplifiedTimeSeriesLoader.loadArff(arffFile);
	   dataset = trainPair.getX();
	   
	   //File arffFile2 = new File(CAR_TEST);
	   File arffFile2 = new File("C:\\Users\\Helen\\Documents\\Uni Informatik\\PG\\AILibs\\tsctestenv\\data\\Downloads\\Car\\Car\\Car_TEST.arff");
	   Pair<TimeSeriesDataset, ClassMapper> testPair = SimplifiedTimeSeriesLoader.loadArff(arffFile2);
	   dataset2 = testPair.getX();
	}
	
	@Test
	public void testPerformance() throws TimeSeriesLoadingException, FileNotFoundException, EvaluationException, TrainingException,
    PredictionException, IOException, ClassNotFoundException {
		double[] alphabet = {2.0,3.0,1.0};
		BOSSClassifier test = new BOSSClassifier(30, 16, alphabet.length, alphabet, false);
		test.setTrainingData(dataset);
		//TODO look after isTrained method.
		try {
			test.train(dataset);
			ArrayList<TimeSeriesDataset> crossSplit = datasetSplit(dataset2); 
			try {
				for(TimeSeriesDataset data : crossSplit) {
					double ownStart = System.currentTimeMillis();
					List<Integer> predictions = test.predict(data);
					double ownEnd = System.currentTimeMillis();
					System.out.println("Time for prediction "+(ownEnd-ownStart));
					int sum = 0;
					int [] targets = data.getTargets();
					for(int i = 0; i < targets.length; i++) {
						if(predictions.get(i) == targets[i]) {
							sum++;
						}
					}
					System.out.println("Accuracy: "+ sum/targets.length);
				}
				
			} catch (PredictionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (TrainingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private ArrayList<TimeSeriesDataset> datasetSplit(TimeSeriesDataset data){
		ArrayList<TimeSeriesDataset> crossVal = new ArrayList<TimeSeriesDataset>();
		for(int i = 0; i < data.getValues(0).length; i++) {
			double[][] matrix = new double[data.getNumberOfInstances()-1][dataset2.getValues(0)[0].length];
			int[] targets = new int[data.getTargets().length-1];
			for(int j = 0; j < data.getValues(0).length; j++) {
				if(j>i) {
					j = j-1;
				}
				if(j!=i) {
					matrix[j] = data.getValues(0)[j];
					targets[j] = data.getTargets()[j];
				}
			}
			ArrayList<double[][]> matrices = new ArrayList<double[][]>();
			matrices.add(matrix);
			TimeSeriesDataset time = new TimeSeriesDataset(matrices, targets);
			crossVal.add(time);
		}
		return crossVal;
	}
}


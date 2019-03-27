package jaicore.ml.tsc.classifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import sfa.timeseries.TimeSeries;
import sfa.timeseries.TimeSeriesLoader;
import sfa.classification.BOSSEnsembleClassifier;
import sfa.classification.Classifier.Predictions;
import sfa.classification.Classifier.Score;

@RunWith(JUnit4.class)
public class BOSSClassifierTest {
	
	  /** Local path for the datasets arff files. */
	private static final String UNIVARIATE_PREFIX = "C:\\Users\\Helen\\Documents\\Uni Informatik\\PG\\AILibs\\tsctestenv\\data\\Downloads\\";

	private static final String CAR_TRAIN = UNIVARIATE_PREFIX + "Car\\Car\\Car_TRAIN.arff";
	private static final String CAR_TEST = UNIVARIATE_PREFIX + "Car\\Car\\Car_TEST.arff";
	
	//private static final String ARROW_TRAIN = 

    /** Dataset used for comparison tests. */
    private TimeSeriesDataset dataset;
    private TimeSeriesDataset dataset2;
		
	@Before
	public void setUp() throws TimeSeriesLoadingException, FileNotFoundException, EvaluationException, TrainingException,
    PredictionException, IOException, ClassNotFoundException {
	   //File arffFile = new File(CAR_TRAIN);
		System.out.println("Hallo");
		//String s = "P:\\Dokumente\\PG\\Classifier_project\\tsctestenv\\data\\Downloads\\Car\\Car\\Car_TRAIN.arff";
		String s = ".\\data\\Downloads\\Car\\Car\\Car_TRAIN.arff";
		String s1 = ".\\data\\Downloads\\Car\\Car\\Car_TEST.arff";
		System.out.println("Hallo");
		File arffFile = new File(s);
	   Pair<TimeSeriesDataset, ClassMapper> trainPair = SimplifiedTimeSeriesLoader.loadArff(arffFile);
	   dataset = trainPair.getX();
	   
	   //File arffFile2 = new File(CAR_TEST);
	   File arffFile2 = new File(s1);
	   Pair<TimeSeriesDataset, ClassMapper> testPair = SimplifiedTimeSeriesLoader.loadArff(arffFile2);
	   dataset2 = testPair.getX();
	}
	
	@Test
	public void testPerformance() throws TimeSeriesLoadingException, FileNotFoundException, EvaluationException, TrainingException,
    PredictionException, IOException, ClassNotFoundException {
		double[] alphabet = {2.0,3.0,1.0,4.0};
		//double[] alphabet = {1.0};
		BOSSClassifier test = new BOSSClassifier(26, 16, alphabet.length, alphabet, true);
		test.setTrainingData(dataset);
		
		
		// Load the train data
		/*
		 * TimeSeries[] trainSamples = TimeSeriesLoader.loadDataset(
		 * "P:\\Dokumente\\PG\\Classifier_project\\tsctestenv\\data\\Downloads\\Car\\Car\\Car.csv"
		 * );
		 * 
		 * BOSSEnsembleClassifier boss = new BOSSEnsembleClassifier(); boss.minF = 6; //
		 * represents the minimal length for training SFA words. default: 6. boss.maxF =
		 * 16; // represents the maximal length for training SFA words. default: 16.
		 * boss.maxS = 4; // symbols of the discretization alphabet. default: 4.
		 * boss.factor = 0.92; // the best models within this factor are kept for
		 * ensembling. default: 0.92
		 * 
		 * // train the BOSS model Score score = boss.fit(trainSamples);
		 * 
		 * 
		 * // Load the test data TimeSeries[] testSamples =
		 * TimeSeriesLoader.loadDataset(
		 * "P:\\Dokumente\\PG\\Classifier_project\\tsctestenv\\data\\Downloads\\Car\\Car\\Car.csv"
		 * );
		 * 
		 * // predict labels Double[] labels = boss.predict(testSamples);
		 * 
		 * 
		 * // predict and score Predictions predictions = boss.score(testSamples);
		 */
		
		
		try {
			long start = System.currentTimeMillis();
			test.train(dataset);
			long end = System.currentTimeMillis();
			System.out.println("Train time: "+(end-start));
			System.out.println("Number of instances "+ dataset.getNumberOfInstances());
			System.out.println("Lenght of instance "+dataset.getValues(0)[0].length);
			int count =  0;
			/*
			 * for(HashMap<Integer,Integer>histo : test.getUnivirateHistograms()) {
			 * System.out.println(count+") "+histo.toString()); count++; }
			 */
			
			//ArrayList<TimeSeriesDataset> crossSplit = datasetSplit(dataset2);
			double sum = 0;
			try {
				
				for(int i = 0; i <dataset2.getValues(0).length; i++) {
					long ownStart = System.currentTimeMillis();
					int prediction = test.predict(dataset2.getValues(0)[i]);
					long ownEnd = System.currentTimeMillis();
					System.out.println("Time for prediction "+(ownEnd-ownStart));
					
					int targets = dataset2.getTargets()[i];
//					System.out.println("original " + targets + " prediction "+prediction);
					if(targets == prediction) {
						sum++;
					}
					
				}
				System.out.println("Accuracy: "+ sum/dataset2.getNumberOfInstances());
				
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
		System.out.println("test2");
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
			System.out.println("new dataset");
			TimeSeriesDataset time = new TimeSeriesDataset(matrices, targets);
			crossVal.add(time);
		}
		return crossVal;
	}
}


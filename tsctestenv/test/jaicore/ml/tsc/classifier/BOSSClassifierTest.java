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
	/*
	 * private TimeSeriesDataset dataset; private TimeSeriesDataset dataset2;
	 */
    ArrayList<TimeSeriesDataset> datasets = new ArrayList<TimeSeriesDataset>();
		
	@Before
	public void setUp() throws TimeSeriesLoadingException, FileNotFoundException, EvaluationException, TrainingException,
    PredictionException, IOException, ClassNotFoundException {
	   //File arffFile = new File(CAR_TRAIN);
		System.out.println("Hallo");
		//String s = "P:\\Dokumente\\PG\\Classifier_project\\tsctestenv\\data\\Downloads\\Car\\Car\\Car_TRAIN.arff";
		String s = ".\\data\\Downloads\\Car\\Car\\Car_TRAIN.arff";
		String s1 = ".\\data\\Downloads\\Car\\Car\\Car_TEST.arff";
		
		String s2 = ".\\data\\Downloads\\ArrowHead\\ArrowHead\\ArrowHead_TRAIN.arff";
		String s3 = ".\\data\\Downloads\\ArrowHead\\ArrowHead\\ArrowHead_TEST.arff";
		
		String s4 = ".\\data\\Downloads\\RacketSports\\RacketSports\\RacketSports_TRAIN.arff";
		String s5 = ".\\data\\Downloads\\RacketSports\\RacketSports\\RacketSports_TEST.arff";
		
		String s6 = ".\\data\\Downloads\\ItalyPowerDemand\\ItalyPowerDemand\\ItalyPowerDemand_TRAIN.arff";
		String s7 = ".\\data\\Downloads\\ItalyPowerDemand\\ItalyPowerDemand\\ItalyPowerDemand_TEST.arff"; 
		
		ArrayList<String> datasetPaths = new ArrayList<String>();
		datasetPaths.add(s);
		datasetPaths.add(s1);
		
		datasetPaths.add(s2);
		datasetPaths.add(s3);
		
		//datasetPaths.add(s4);
		//datasetPaths.add(s5);
		
		datasetPaths.add(s6);
		datasetPaths.add(s7);
		
		
		
		for(String str : datasetPaths) {
			File arffile = new File(str);
			Pair<TimeSeriesDataset, ClassMapper> pair = SimplifiedTimeSeriesLoader.loadArff(arffile);
			TimeSeriesDataset dataset = pair.getX();
			
			datasets.add(dataset);
		}
		/*
		 * System.out.println("Hallo"); File arffFile = new File(s);
		 * Pair<TimeSeriesDataset, ClassMapper> trainPair =
		 * SimplifiedTimeSeriesLoader.loadArff(arffFile); dataset = trainPair.getX();
		 * 
		 * //File arffFile2 = new File(CAR_TEST); File arffFile2 = new File(s1);
		 * Pair<TimeSeriesDataset, ClassMapper> testPair =
		 * SimplifiedTimeSeriesLoader.loadArff(arffFile2); dataset2 = testPair.getX();
		 */
	}
	
	@Test
	public void testPerformance() throws TimeSeriesLoadingException, FileNotFoundException, EvaluationException, TrainingException,
    PredictionException, IOException, ClassNotFoundException {
		ArrayList<double[]> alphabets = new ArrayList<double[]>();
		double[] alphabet1 = {2.0,3.0,1.0,4.0};
		double[] alphabet2 = {2.0,1.0};
		double[] alphabet3 = {2.0,3.0,1.0,4.0,5.0};
		alphabets.add(alphabet1);
		alphabets.add(alphabet2);
		alphabets.add(alphabet3);
		
		int [] windowLength = {10,20,30};
		int [] wordLength = {8,10,12,16};
		
		double[] bestAccus = new double[4];
		int count = 0;
		
		ArrayList<double[][]> bestfoundInfos = new ArrayList<double[][]>();
		
		ArrayList<ArrayList<Double>> foundAccus = new ArrayList<ArrayList<Double>>();
		
		boolean[] mean = {true,false};
		//double[] alphabet = {1.0};
		
		for(int i = 0; i < datasets.size()-1; i+=2) {
			ArrayList<Double> localfoundAccus = new ArrayList<Double>();
			System.out.println("------------------------------------------------------------");
			System.out.println("NEW DATASET");
			double [][] bestfoundInfo = new double [4][5];
			double bestfound = 0;
			for(double[] alphabet : alphabets) {
				for(int window : windowLength) {
					for(int word : wordLength) {
						for(boolean m : mean) {
							int instancelength = datasets.get(i).getValues(0)[0].length;
							if(window < instancelength) {
							if(word < window) {
								BOSSClassifier test = new BOSSClassifier(window, word, alphabet.length, alphabet, m);
								test.setTrainingData(datasets.get(i));
								
								
								try {
									long start = System.currentTimeMillis();
									test.train(datasets.get(i));
									long end = System.currentTimeMillis();
									System.out.println("Alphabet "+Arrays.toString(alphabet) + " window length "+ window + " word length "+ word + " mean correctd "+ m);
									
									System.out.println("Train time: "+(end-start));
									//System.out.println("Number of instances "+ dataset.getNumberOfInstances());
									//System.out.println("Lenght of instance "+dataset.getValues(0)[0].length);
									//int count =  0;
								
									double sum = 0;
									try {
										
										for(int j = 0; j <datasets.get(i+1).getValues(0).length; j++) {
//											long ownStart = System.currentTimeMillis();
											int prediction = test.predict(datasets.get(i+1).getValues(0)[j]);
											/*
											 * long ownEnd = System.currentTimeMillis();
											 * System.out.println("Time for prediction "+(ownEnd-ownStart));
											 */
											
											int targets = datasets.get(i+1).getTargets()[j];
//											System.out.println("original " + targets + " prediction "+prediction);
											if(targets == prediction) {
												sum++;
											}
											
										}
										
										double accu = sum/datasets.get(i+1).getNumberOfInstances();
										localfoundAccus.add(accu);
										System.out.println("Accuracy: "+ accu);
										if(accu > bestfound) {
											bestfound = accu;
											int t = 0;
											for(double d : alphabet) {
													bestfoundInfo[0][t] = d;
													t++;
												
											}
											bestfoundInfo[1][0] = window;
											bestfoundInfo[2][0] = word;
											if(m) {
												bestfoundInfo[3][0] = 1;
											}
											
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
							}
						}
					}
				}
			}
			
			bestAccus[count] = bestfound;
			bestfoundInfos.add(bestfoundInfo);
			foundAccus.add(localfoundAccus);
			count++;
		}
		
		System.out.println("Best accus for datasets "+ Arrays.toString(bestAccus));
		for(double [][] d : bestfoundInfos) {
			System.out.println("With this configs: Alphabet "+ Arrays.toString(d[0])+" window length "+ d[1][0]+ " word length "+ d[2][0] + " mean correction "+ d[3][0]);
		}
		
		System.out.println("Average accu ");
		
		for(ArrayList<Double> d : foundAccus) {
			double sum = 0;
			for(double doub : d ) {
				sum +=doub;
			}
			sum  = sum/d.size();
			System.out.println(sum);
		}
		
		
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
		
		
		
		
	}
	
	/*
	 * private ArrayList<TimeSeriesDataset> datasetSplit(TimeSeriesDataset data){
	 * ArrayList<TimeSeriesDataset> crossVal = new ArrayList<TimeSeriesDataset>();
	 * System.out.println("test2"); for(int i = 0; i < data.getValues(0).length;
	 * i++) { double[][] matrix = new
	 * double[data.getNumberOfInstances()-1][dataset2.getValues(0)[0].length]; int[]
	 * targets = new int[data.getTargets().length-1]; for(int j = 0; j <
	 * data.getValues(0).length; j++) { if(j>i) { j = j-1; } if(j!=i) { matrix[j] =
	 * data.getValues(0)[j]; targets[j] = data.getTargets()[j]; } }
	 * ArrayList<double[][]> matrices = new ArrayList<double[][]>();
	 * matrices.add(matrix); System.out.println("new dataset"); TimeSeriesDataset
	 * time = new TimeSeriesDataset(matrices, targets); crossVal.add(time); } return
	 * crossVal; }
	 */
}


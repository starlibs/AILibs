package jaicore.ml.dyadranking.general;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.algorithm.ADyadRanker;
import jaicore.ml.dyadranking.algorithm.APLDyadRanker;
import jaicore.ml.dyadranking.algorithm.IPLNetDyadRankerConfiguration;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;
import jaicore.ml.dyadranking.loss.DyadRankingLossUtil;
import jaicore.ml.dyadranking.loss.KendallsTauDyadRankingLoss;
import jaicore.ml.dyadranking.loss.KendallsTauOfTopK;
import jaicore.ml.dyadranking.util.DyadStandardScaler;

/**
 * This is a test based on Dirk Schäfers dyad ranking dataset based on
 * performance data of genetic algorithms on traveling salesman problem
 * instances https://github.com/disc5/ga-tsp-dataset which was used in [1] for
 * evaluation.
 * 
 * [1] Schäfer, D., & Hüllermeier, E. (2015). Dyad Ranking using a Bilinear
 * {P}lackett-{L}uce Model. In Proceedings ECML/PKDD--2015, European Conference
 * on Machine Learning and Knowledge Discovery in Databases (pp. 227–242).
 * Porto, Portugal: Springer.
 * 
 * @author Jonas Hanselle
 *
 */
@RunWith(Parameterized.class)
public class DyadRankerGATSPTest {

	private static final String XXL_FILE = "testsrc/ml/dyadranking/ga-tsp/data_meta/GAMeta72-LR.txt";
	private static final String ALTERNATIVES_FEATURE_FILE = "testsrc/ml/dyadranking/ga-tsp/data_meta/GAMeta72-labeldescriptions.csv";
	private static final String ORDERINGS_FILE = "testsrc/ml/dyadranking/ga-tsp/data_meta/orderings.csv";

	// M = average ranking length
	private static final int M = 30;
	// N = number of training instances
	private static final int N = 120;
	// seed for shuffling the dataset
	private static final long seed = 15;

	ADyadRanker ranker;
	DyadRankingDataset dataset;

	public DyadRankerGATSPTest(ADyadRanker ranker) {
		this.ranker = ranker;
	}

	@Before
	public void init() {
		// load dataset
		dataset = loadDatasetFromXXLAndCSV();
	}

	@Test
	public void test() {

		Collections.shuffle(dataset, new Random(seed));

		// split data
		DyadRankingDataset trainData = new DyadRankingDataset(dataset.subList(0, N));
		DyadRankingDataset testData = new DyadRankingDataset(dataset.subList(N, dataset.size()));
		
		// trim dyad ranking instances for train data
		trainData = randomlyTrimSparseDyadRankingInstances(trainData, M);

		// standardize data
		DyadStandardScaler scaler = new DyadStandardScaler();
		scaler.fit(trainData);
		scaler.transformInstances(trainData);
		scaler.transformInstances(testData);

		KendallsTauOfTopK ktotk = new KendallsTauOfTopK(15, 0.75);
		System.out.println(ktotk.loss(trainData.get(0), trainData.get(0)));
		
//		try {
//
//			// train the ranker
//			ranker.train(trainData);
//			double avgKendallTau = 0.0d;
//			avgKendallTau = DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(), testData, ranker);
//			List<IDyadRankingInstance> predictions = ranker.predict(testData);
//			KendallsTauOfTopK ktotk = new KendallsTauOfTopK(15, Math.PI);
//			for(int i = 0; i < testData.size(); i++) {
//				System.out.println(ktotk.loss(testData.get(i), testData.get(i)));
//			}
//			System.out.println("Average Kendall's tau for " + ranker.getClass().getSimpleName() + ": " + avgKendallTau);
//			assertTrue(avgKendallTau > 0.5d);
//		} catch (TrainingException | PredictionException e) {
//			e.printStackTrace();
//		}

	}

	/**
	 * Loads the dataset from the xxl and the csv files.
	 * 
	 * @return {@link DyadRankingDataset} constructed of the instances and
	 *         alternatives in the corresponding files
	 */
	private static DyadRankingDataset loadDatasetFromXXLAndCSV() {

		DyadRankingDataset dataset = new DyadRankingDataset();

		// this is a bit messy and hand tailored towards the input we expect
		int[][] orderings = new int[246][72];

		List<List<String>> records = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(ORDERINGS_FILE))) {
			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				int j = 0;
				String[] values = line.split(",");
				records.add(Arrays.asList(values));
				for (String value : values) {
					orderings[i][j] = Integer.parseInt(value);
					j++;
				}
				i++;
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		List<Vector> alternativeFeatures = new ArrayList<Vector>(100);

		// parse the file containing the features of the alternatives
		File alternativeFile = new File(ALTERNATIVES_FEATURE_FILE);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(alternativeFile));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split(",");
				DenseDoubleVector vector = new DenseDoubleVector(tokens.length);
				for (int i = 0; i < vector.length(); i++) {
					vector.setValue(i, Double.parseDouble(tokens[i]));
				}
				alternativeFeatures.add(vector);
			}
			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// parse XXL file
		File xxlFile = new File(XXL_FILE);
		int numAttributes = 0;
		int numLabels = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(xxlFile));
			// read the first line and setup counters accordingly
			String line = reader.readLine();
			String[] tokens = line.split("\t");
			for (String token : tokens) {
				switch (token.charAt(0)) {
				case 'A':
					numAttributes++;
					break;
				case 'L':
					numLabels++;
					break;
				}
			}

			// skip two lines
			reader.readLine();
			reader.readLine();

			List<Vector> instanceFeatures = new ArrayList<Vector>(246);
			List<ArrayList<Vector>> alternativesList = new ArrayList<ArrayList<Vector>>(246);
			int lineIndex = 0;
			while ((line = reader.readLine()) != null) {
				tokens = line.split("\t");
				Vector instance = new DenseDoubleVector(numAttributes);
				ArrayList<Vector> alternatives = new ArrayList<Vector>(numLabels);

				// add the instances to the dyad ranking instance
				for (int i = 0; i < numAttributes; i++) {
					double val = Double.parseDouble(tokens[i]);
					instance.setValue(i, val);
				}

				// add the alternatives to the dyad ranking instance
				for (int i = numAttributes; i < tokens.length; i++) {
					int index = orderings[lineIndex][i - numAttributes] - 1;
					alternatives.add(alternativeFeatures.get(index));
				}
				instanceFeatures.add(instance);
				alternativesList.add(alternatives);

			}

			for (int i = 0; i < instanceFeatures.size(); i++) {
				dataset.add(new SparseDyadRankingInstance(instanceFeatures.get(i), alternativesList.get(i)));
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dataset;
	}

	/**
	 * Trims the sparse dyad ranking instances by randomly selecting alternatives
	 * from each dyad ranking instance.
	 * 
	 * @param dataset
	 * @param dyadRankingLength the length of the trimmed dyad ranking instances
	 * @param seed
	 * @return
	 */
	private static DyadRankingDataset randomlyTrimSparseDyadRankingInstances(DyadRankingDataset dataset,
			int dyadRankingLength) {
		DyadRankingDataset trimmedDataset = new DyadRankingDataset();
		for (IInstance instance : dataset) {
			IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;
			if (drInstance.length() < dyadRankingLength)
				continue;
			ArrayList<Boolean> flagVector = new ArrayList<Boolean>(drInstance.length());
			for (int i = 0; i < dyadRankingLength; i++) {
				flagVector.add(Boolean.TRUE);
			}
			for (int i = dyadRankingLength; i < drInstance.length(); i++) {
				flagVector.add(Boolean.FALSE);
			}
			Collections.shuffle(flagVector);
			List<Vector> trimmedAlternatives = new ArrayList<Vector>(dyadRankingLength);
			for (int i = 0; i < drInstance.length(); i++) {
				if (flagVector.get(i))
					trimmedAlternatives.add(drInstance.getDyadAtPosition(i).getAlternative());
			}
			SparseDyadRankingInstance trimmedDRInstance = new SparseDyadRankingInstance(
					drInstance.getDyadAtPosition(0).getInstance(), trimmedAlternatives);
			trimmedDataset.add(trimmedDRInstance);
		}
		return trimmedDataset;
	}

	@Parameters
	public static List<APLDyadRanker> supplyDyadRankers() {
		PLNetDyadRanker plNetRanker = new PLNetDyadRanker();
		// Use a simple config such that the test finishes quickly
		plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_ACTIVATION_FUNCTION, "SIGMOID");
		plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_PLNET_HIDDEN_NODES, "5");
		plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_MAX_EPOCHS, "10");
		plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_INTERVAL, "1");
		plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_PATIENCE, "5");
		plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_RETRAIN, "false");
		plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_PLNET_LEARNINGRATE, "0.1");
		plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_MINI_BATCH_SIZE, "1");
		return Arrays.asList(plNetRanker);
	}
}
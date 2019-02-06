package jaicore.ml.dyadranking.activelearning;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.nd4j.linalg.primitives.Pair;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.dyadranking.Dyad;
import jaicore.ml.dyadranking.algorithm.APLDyadRanker;
import jaicore.ml.dyadranking.algorithm.IPLNetDyadRankerConfiguration;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;
import jaicore.ml.dyadranking.loss.DyadRankingLossUtil;
import jaicore.ml.dyadranking.loss.KendallsTauDyadRankingLoss;

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
public class ActiveDyadRankingMetaminingTest {

	private static final String DYADS_SCORES_FILE = "testsrc/ml/dyadranking/meta-mining-dyads.txt";

	private static final double trainRatio = 0.7d;

	// N = number of training instances
	private static final int N = 120;
	// seed for shuffling the dataset
	private static final long seed = 1337;

	PLNetDyadRanker ranker;
	DyadRankingDataset dataset;

	public ActiveDyadRankingMetaminingTest(PLNetDyadRanker ranker) {
		this.ranker = ranker;
	}

	@Before
	public void init() {
	}

	@Test
	public void test() {
//			dataset = randomlyTrimSparseDyadRankingInstances(dataset, 5);

		// split data

		// standardize data
//		DyadStandardScaler scaler = new DyadStandardScaler();
//		scaler.fit(trainData);
//		scaler.transformInstances(trainData);
//		scaler.transformInstances(testData);

//			try {
//				ranker.train(new DyadRankingDataset( trainData.subList(0, 5)));
//			} catch (TrainingException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}

//		DyadDatasetPoolProvider poolProvider = new DyadDatasetPoolProvider(trainData);

		List<Pair<Dyad, Double>> dyadScorePairs = loadDyadsAndScore(DYADS_SCORES_FILE);

		DyadScorePoolProvider poolProvider = new DyadScorePoolProvider(dyadScorePairs);
		DyadRankingDataset dataset = new DyadRankingDataset();
		for (Vector vector : poolProvider.getInstanceFeatures()) {
			dataset.add(poolProvider.getDyadRankingInstanceForInstanceFeatures(vector));
		}

		Collections.shuffle(dataset, new Random(seed));

		DyadRankingDataset trainData = new DyadRankingDataset(dataset.subList(0, (int) (trainRatio * dataset.size())));
		DyadRankingDataset testData = new DyadRankingDataset(
				dataset.subList((int) (trainRatio * dataset.size()), dataset.size()));

		System.out.println("size before: " + poolProvider.getInstanceFeatures().size());

		System.out.println("train data: ");
		for (IInstance instance : trainData) {
			IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;
			System.out.println(drInstance.getDyadAtPosition(0).getInstance());

		}

		System.out.println("test data: ");
		for (IInstance instance : testData) {
			IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;
			System.out.println(drInstance.getDyadAtPosition(0).getInstance());
		}

		for (IInstance instance : testData) {
			IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;

			poolProvider.removeDyadsFromPoolByInstances(drInstance.getDyadAtPosition(0).getInstance());

		}

		System.out.println("size after: " + poolProvider.getInstanceFeatures().size());

//		ActiveDyadRanker activeRanker = new PrototypicalPoolBasedActiveDyadRanker(ranker, poolProvider);
		ActiveDyadRanker activeDyadRanker = new RandomPoolBasedActiveDyadRanker(ranker, poolProvider, seed);
		
		try {

			// train the ranker
			for (IInstance inst : trainData)
				System.out.println(((IDyadRankingInstance) inst).getDyadAtPosition(0).getInstance());
			for (int i = 0; i < 100; i++) {
				activeDyadRanker.activelyTrain(1);
				double avgKendallTau = DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(),
						testData, ranker);
				System.out.print(avgKendallTau + ",");
			}

//				double avgKendallTau = 0.0d;
//				System.out.println("Average Kendall's tau for " + ranker.getClass().getSimpleName() + ": " + avgKendallTau);
//				assertTrue(avgKendallTau > 0.5d);
		} catch (Exception e) {
			e.printStackTrace();
		}

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

	private List<Pair<Dyad, Double>> loadDyadsAndScore(String filePath) {
		List<Pair<Dyad, Double>> dyadScorePairs = new LinkedList<Pair<Dyad, Double>>();
		try {
			FileInputStream in = new FileInputStream(new File(filePath));
			String input = IOUtils.toString(in, StandardCharsets.UTF_8);
			String[] rows = input.split("\n");
			for (String row : rows) {
				if (row.isEmpty())
					break;
				List<Dyad> dyads = new LinkedList<Dyad>();
				String[] dyadTokens = row.split("\\|");
				String dyadString = dyadTokens[0];
				String[] values = dyadString.split(";");
				if (values[0].length() > 1 && values[1].length() > 1) {
					String[] instanceValues = values[0].substring(1, values[0].length() - 1).split(",");
					String[] alternativeValues = values[1].substring(1, values[1].length() - 1).split(",");
					Vector instance = new DenseDoubleVector(instanceValues.length);
					for (int i = 0; i < instanceValues.length; i++) {
						instance.setValue(i, Double.parseDouble(instanceValues[i]));
					}

					Vector alternative = new DenseDoubleVector(alternativeValues.length);
					for (int i = 0; i < alternativeValues.length; i++) {
						alternative.setValue(i, Double.parseDouble(alternativeValues[i]));
					}
					Dyad dyad = new Dyad(instance, alternative);

					Double score = Double.parseDouble(dyadTokens[1]);

					dyadScorePairs.add(new Pair<Dyad, Double>(dyad, score));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dyadScorePairs;
	}
}

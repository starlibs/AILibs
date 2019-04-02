package jaicore.ml.dyadranking.activelearning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import jaicore.ml.dyadranking.algorithm.IPLDyadRanker;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.loss.DyadRankingLossUtil;
import jaicore.ml.dyadranking.loss.KendallsTauDyadRankingLoss;
import jaicore.ml.dyadranking.util.DyadStandardScaler;
import weka.clusterers.SimpleKMeans;

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
public class ActiveDyadRankingGATSPTest {

	private static final String GATSP_DATASET_FILE = "testrsc/ml/dyadranking/ga-tsp/GATSP-Data.txt";

	// N = number of training instances
	private static final int N = 120;
	// seed for shuffling the dataset

	PLNetDyadRanker ranker;
	DyadRankingDataset dataset;

	public ActiveDyadRankingGATSPTest(PLNetDyadRanker ranker) {
		this.ranker = ranker;
	}

	@Before
	public void init() {
		// load dataset
		dataset = new DyadRankingDataset();
		try {
			dataset.deserialize(new FileInputStream(new File(GATSP_DATASET_FILE)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() {
		int seed = 0;

		Collections.shuffle(dataset, new Random(seed));

		// split data
		DyadRankingDataset trainData = new DyadRankingDataset(dataset.subList(0, N));
		DyadRankingDataset testData = new DyadRankingDataset(dataset.subList(N, dataset.size()));

		// standardize data
		DyadStandardScaler scaler = new DyadStandardScaler();
		scaler.fit(trainData);
		scaler.transformInstances(trainData);
		scaler.transformInstances(testData);

		DyadDatasetPoolProvider poolProvider = new DyadDatasetPoolProvider(trainData);
		poolProvider.setRemoveDyadsWhenQueried(false);

		SimpleKMeans clusterer = new SimpleKMeans();
		try {
			clusterer.setNumClusters(5);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		ConfidenceIntervalClusteringBasedActiveDyadRanker activeRanker = new ConfidenceIntervalClusteringBasedActiveDyadRanker(
				ranker, poolProvider, seed, 5, 5, clusterer);

		List<ActiveDyadRanker> activeRankers = new ArrayList<>();
		activeRankers.add(activeRanker);
		activeRankers.add(new UCBPoolBasedActiveDyadRanker(new PLNetDyadRanker(),
				new DyadDatasetPoolProvider(trainData), seed, 5, 5));
		activeRankers.add(new PrototypicalPoolBasedActiveDyadRanker(new PLNetDyadRanker(),
				new DyadDatasetPoolProvider(trainData), 5, 5, 0.0d, 5, seed));
		activeRankers.add(new RandomPoolBasedActiveDyadRanker(new PLNetDyadRanker(),
				new DyadDatasetPoolProvider(trainData), seed, 5));

		for (ActiveDyadRanker curActiveRanker : activeRankers) {
			try {

				// train the ranker
				for (int i = 0; i < 10; i++) {
					curActiveRanker.activelyTrain(1);
					double avgKendallTau = DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(),
							testData, curActiveRanker.getRanker());
					double avgKendallTauIS = DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(),
							new DyadRankingDataset(poolProvider.getQueriedRankings()), curActiveRanker.getRanker());
					System.out.println("Current Kendalls Tau: " + avgKendallTau);
					System.out.println("Current Kendalls Tau IS: " + avgKendallTauIS);
				}
				double avgKendallTau = DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(),
						testData, curActiveRanker.getRanker());
				Assert.assertTrue(avgKendallTau > 0.0d);

			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("final results: ");
		}
	}

	@Parameters
	public static List<IPLDyadRanker> supplyDyadRankers() {
		PLNetDyadRanker plNetRanker = new PLNetDyadRanker();
		// Use a simple config such that the test finishes quickly
		return Arrays.asList(plNetRanker);
	}
}

package ai.libs.jaicore.ml.weka.ranking.dyad.activelearning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.ml.ranking.dyad.DyadRankingLossUtil;
import ai.libs.jaicore.ml.ranking.dyad.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.ranking.dyad.learner.activelearning.ActiveDyadRanker;
import ai.libs.jaicore.ml.ranking.dyad.learner.activelearning.DyadDatasetPoolProvider;
import ai.libs.jaicore.ml.ranking.dyad.learner.activelearning.PrototypicalPoolBasedActiveDyadRanker;
import ai.libs.jaicore.ml.ranking.dyad.learner.activelearning.RandomPoolBasedActiveDyadRanker;
import ai.libs.jaicore.ml.ranking.dyad.learner.activelearning.UCBPoolBasedActiveDyadRanker;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.PLNetDyadRanker;
import ai.libs.jaicore.ml.ranking.dyad.learner.util.DyadStandardScaler;
import ai.libs.jaicore.ml.ranking.loss.KendallsTauDyadRankingLoss;
import ai.libs.jaicore.ml.weka.ranking.dyad.learner.activelearning.ConfidenceIntervalClusteringBasedActiveDyadRanker;
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
public class ActiveDyadRankingGATSPTest {

	private static final String GATSP_DATASET_FILE = "testrsc/ml/dyadranking/ga-tsp/GATSP-Data.txt";

	// N = number of training instances
	private static final int N = 120;
	// seed for shuffling the dataset

	DyadRankingDataset dataset;

	@BeforeEach
	public void init() throws FileNotFoundException {
		// load dataset
		this.dataset = new DyadRankingDataset();
		this.dataset.deserialize(new FileInputStream(new File(GATSP_DATASET_FILE)));
	}

	@ParameterizedTest
	@Disabled
	@MethodSource("supplyDyadRankers")
	public void test(final PLNetDyadRanker ranker) throws Exception {
		int seed = 0;

		Collections.shuffle(this.dataset, new Random(seed));

		// split data
		System.out.println(this.dataset.size());
		DyadRankingDataset trainData = new DyadRankingDataset(this.dataset.subList(0, N));
		DyadRankingDataset testData = new DyadRankingDataset(this.dataset.subList(N, this.dataset.size()));

		// standardize data
		DyadStandardScaler scaler = new DyadStandardScaler();
		scaler.fit(trainData);
		scaler.transformInstances(trainData);
		scaler.transformInstances(testData);

		DyadDatasetPoolProvider poolProvider = new DyadDatasetPoolProvider(trainData);
		poolProvider.setRemoveDyadsWhenQueried(false);

		SimpleKMeans clusterer = new SimpleKMeans();
		clusterer.setNumClusters(5);

		ConfidenceIntervalClusteringBasedActiveDyadRanker activeRanker = new ConfidenceIntervalClusteringBasedActiveDyadRanker(ranker, poolProvider, seed, 5, 5, clusterer);

		List<ActiveDyadRanker> activeRankers = new ArrayList<>();
		activeRankers.add(activeRanker);
		activeRankers.add(new UCBPoolBasedActiveDyadRanker(new PLNetDyadRanker(), new DyadDatasetPoolProvider(trainData), seed, 5, 5));
		activeRankers.add(new PrototypicalPoolBasedActiveDyadRanker(new PLNetDyadRanker(), new DyadDatasetPoolProvider(trainData), 5, 5, 0.0d, 5, seed));
		activeRankers.add(new RandomPoolBasedActiveDyadRanker(new PLNetDyadRanker(), new DyadDatasetPoolProvider(trainData), seed, 5));

		for (ActiveDyadRanker curActiveRanker : activeRankers) {
			// train the ranker
			for (int i = 0; i < 10; i++) {
				curActiveRanker.activelyTrain(1);
				double avgKendallTau = DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(), testData, curActiveRanker.getRanker());
				double avgKendallTauIS = DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(), new DyadRankingDataset(poolProvider.getQueriedRankings()), curActiveRanker.getRanker());
				System.out.println("Current Kendalls Tau: " + avgKendallTau);
				System.out.println("Current Kendalls Tau IS: " + avgKendallTauIS);
			}
			double avgKendallTau = DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(), testData, curActiveRanker.getRanker());
			Assert.assertTrue(avgKendallTau > 0.0d);

			System.out.println("final results: ");
		}
	}

	public static Stream<Arguments> supplyDyadRankers() {
		// Use a simple config such that the test finishes quickly
		PLNetDyadRanker plNetRanker = new PLNetDyadRanker();
		return Stream.of(Arguments.of(plNetRanker));
	}
}

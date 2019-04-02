package jaicore.ml.dyadranking.activelearning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import jaicore.ml.dyadranking.algorithm.APLDyadRanker;
import jaicore.ml.dyadranking.algorithm.IPLNetDyadRankerConfiguration;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.loss.DyadRankingLossUtil;
import jaicore.ml.dyadranking.loss.KendallsTauDyadRankingLoss;
import jaicore.ml.dyadranking.util.AbstractDyadScaler;
import jaicore.ml.dyadranking.util.DyadMinMaxScaler;

/**
 * Test for active dyad ranking based on AutomL metamining data.
 * 
 * @author Jonas Hanselle
 *
 */
@RunWith(Parameterized.class)
public class ActiveDyadRankingMetaminingTest {

	private static final String META_DATA_DATASET_FILE = "testsrc/ml/dyadranking/ga-tsp/MLPlan-Data.txt";

	private static final double TRAIN_RATIO = 0.7d;

	private static final int MAX_BATCH_SIZE = 5;
	private static final int TOP_RANKING_LENGTH = 5;
	private static final double RATIO_OF_OLD_SAMPLES_IN_MINIBATCH = 0.0d;
	
	private static int seed = 443;


	PLNetDyadRanker ranker;
	DyadRankingDataset dataset;

	public ActiveDyadRankingMetaminingTest(PLNetDyadRanker ranker) {

	}

	@Before
	public void init() {
		// load dataset
		dataset = new DyadRankingDataset();
		try {
			dataset.deserialize(new FileInputStream(new File(META_DATA_DATASET_FILE)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() {

		// split data
		DyadRankingDataset trainData = new DyadRankingDataset(dataset.subList(0, (int) (dataset.size() * TRAIN_RATIO)));
		DyadRankingDataset testData = new DyadRankingDataset(
				dataset.subList((int) (dataset.size() * TRAIN_RATIO), dataset.size()));

		// standardize data
		AbstractDyadScaler scaler = new DyadMinMaxScaler();
		scaler.fit(trainData);
		scaler.transformInstances(trainData);
		scaler.transformInstances(testData);
		ranker = new PLNetDyadRanker();
		DyadDatasetPoolProvider poolProvider = new DyadDatasetPoolProvider(trainData);
		poolProvider.setRemoveDyadsWhenQueried(false);
		ActiveDyadRanker activeDyadRanker = new RandomPoolBasedActiveDyadRanker(ranker, poolProvider, seed, 0);


		SummaryStatistics[] results = new SummaryStatistics[100];
		for (int i = 0; i < results.length; i++) {
			results[i] = new SummaryStatistics();
		}

		try {

			for (int i = 0; i < 10; i++) {
				activeDyadRanker.activelyTrain(1);
				double avgKendallTau = DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(),
						testData, ranker);
				results[i].addValue(avgKendallTau);
				System.out.print(avgKendallTau + ",");
			}
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
		plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_MAX_EPOCHS, "100");
		plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_INTERVAL, "1");
		plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_PATIENCE, "5");
		plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_RETRAIN, "false");
		plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_PLNET_LEARNINGRATE, "0.1");
		plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_MINI_BATCH_SIZE, "1");
		plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_PLNET_SEED, Long.toString(seed));
		plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_TRAIN_RATIO, "0.8");

		return Arrays.asList(plNetRanker);
	}

}

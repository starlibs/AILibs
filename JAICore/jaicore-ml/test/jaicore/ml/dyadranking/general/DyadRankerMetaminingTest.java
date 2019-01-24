package jaicore.ml.dyadranking.general;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.algorithm.ADyadRanker;
import jaicore.ml.dyadranking.algorithm.APLDyadRanker;
import jaicore.ml.dyadranking.algorithm.IPLNetDyadRankerConfiguration;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.loss.DyadRankingLossUtil;
import jaicore.ml.dyadranking.loss.KendallsTauDyadRankingLoss;
import jaicore.ml.dyadranking.util.DyadStandardScaler;

/**
 * This is a test based on a dataset containing 400 dyad rankings of dataset and
 * pipeline metafeatures obtain from ML-Plan via landmarking and treemining.
 * Note that the train and test data are drawn from the same pool, i.e. train
 * and test data likely contain dyads from the same classifiers on the same
 * datasets. This unit test is intended to test the functionality of the dyad
 * ranker. It is NOT intended to give an unbiased estimate of the rankers
 * performance in a real-world scenario.
 * 
 * @author Jonas Hanselle
 *
 */
@RunWith(Parameterized.class)
public class DyadRankerMetaminingTest {

	private static final String DATASET_FILE = "testsrc/ml/dyadranking/metamining-dataset.txt";

	// N = number of training instances
	private static final int N = 300;
	// seed for shuffling the dataset
	private static final long seed = 15;

	ADyadRanker ranker;
	DyadRankingDataset dataset;

	public DyadRankerMetaminingTest(ADyadRanker ranker) {
		this.ranker = ranker;
	}

	@Before
	public void init() {
		// load dataset
		dataset = new DyadRankingDataset();
		try {
			dataset.deserialize(new FileInputStream(new File(DATASET_FILE)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void test() {

		DyadStandardScaler scaler = new DyadStandardScaler();
		Collections.shuffle(dataset, new Random(seed));

		// split data
		DyadRankingDataset trainData = new DyadRankingDataset(dataset.subList(0, N));
		DyadRankingDataset testData = new DyadRankingDataset(dataset.subList(N, dataset.size()));
		// standardize data
		scaler.fit(trainData);
		scaler.transformInstances(trainData);
		scaler.transformInstances(testData);

		try {

			// train the ranker
			ranker.train(trainData);
			double avgKendallTau = 0.0d;
			avgKendallTau = DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(), testData, ranker);
			System.out.println("Average Kendall's tau for " + ranker.getClass().getSimpleName() + ": " + avgKendallTau);
			assertTrue(avgKendallTau > 0.5d);
		} catch (TrainingException | PredictionException e) {
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
}

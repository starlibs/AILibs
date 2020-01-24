package ai.libs.jaicore.ml.ranking.dyad.general;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.api4.java.common.math.IVector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.jaicore.ml.ranking.dyad.DyadRankingLossUtil;
import ai.libs.jaicore.ml.ranking.dyad.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.ranking.dyad.dataset.SparseDyadRankingInstance;
import ai.libs.jaicore.ml.ranking.dyad.learner.Dyad;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.PLNetDyadRanker;
import ai.libs.jaicore.ml.ranking.dyad.learner.util.AbstractDyadScaler;
import ai.libs.jaicore.ml.ranking.dyad.learner.util.DyadUnitIntervalScaler;
import ai.libs.jaicore.ml.ranking.loss.KendallsTauDyadRankingLoss;

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

	private static final String DATASET_FILE = "testrsc/ml/dyadranking/MLPlan-Data.txt";

	// N = number of training instances
	private static final int N = 300;
	// seed for shuffling the dataset
	private static final long seed = 15;

	PLNetDyadRanker ranker;
	DyadRankingDataset dataset;

	public DyadRankerMetaminingTest(final PLNetDyadRanker ranker) {
		this.ranker = ranker;
	}

	@Before
	public void init() throws FileNotFoundException {
		// load dataset
		this.dataset = new DyadRankingDataset();
		this.dataset.deserialize(new FileInputStream(new File(DATASET_FILE)));
	}

	@Test
	public void test() throws InterruptedException, TrainingException, PredictionException {

		AbstractDyadScaler scaler = new DyadUnitIntervalScaler();
		Collections.shuffle(this.dataset, new Random(seed));

		// split data
		DyadRankingDataset trainData = new DyadRankingDataset(this.dataset.subList(0, (int) (0.7 * this.dataset.size())));
		DyadRankingDataset testData = new DyadRankingDataset(this.dataset.subList((int) (0.7 * this.dataset.size()), this.dataset.size()));

		// standardize data
		// scaler.fit(trainData);
		// scaler.transformAlternatives(trainData);
		// scaler.transformAlternatives(testData);

		// trainData = randomlyTrimSparseDyadRankingInstances(trainData, 2);
		// testData = randomlyTrimSparseDyadRankingInstances(testData, 5);


		// train the ranker
		this.ranker.fit(trainData);
		double avgKendallTau = 0.0d;
		avgKendallTau = DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(), testData, this.ranker);
		System.out.println("Average Kendall's tau for " + this.ranker.getClass().getSimpleName() + ": " + avgKendallTau);
		assertTrue(avgKendallTau > 0.5d);
		IDyadRankingInstance drInstance = testData.get(0);
		List<Dyad> dyads = new LinkedList<>();

	}

	@Parameters
	public static List<PLNetDyadRanker> supplyDyadRankers() {
		PLNetDyadRanker plNetRanker = new PLNetDyadRanker();
		// Use a simple config such that the test finishes quickly
		// plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_ACTIVATION_FUNCTION, "SIGMOID");
		// plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_PLNET_HIDDEN_NODES, "5");
		// plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_MAX_EPOCHS, "10");
		// plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_INTERVAL, "1");
		// plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_PATIENCE, "5");
		// plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_RETRAIN, "false");
		// plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_PLNET_LEARNINGRATE, "0.1");
		// plNetRanker.getConfiguration().setProperty(IPLNetDyadRankerConfiguration.K_MINI_BATCH_SIZE, "1");
		return Arrays.asList(plNetRanker);
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
	private static DyadRankingDataset randomlyTrimSparseDyadRankingInstances(final DyadRankingDataset dataset, final int dyadRankingLength) {
		DyadRankingDataset trimmedDataset = new DyadRankingDataset();
		for (IDyadRankingInstance instance : dataset) {
			if (instance.getNumAttributes() < dyadRankingLength) {
				continue;
			}
			ArrayList<Boolean> flagVector = new ArrayList<>(instance.getNumAttributes());
			for (int i = 0; i < dyadRankingLength; i++) {
				flagVector.add(Boolean.TRUE);
			}
			for (int i = dyadRankingLength; i < instance.getNumAttributes(); i++) {
				flagVector.add(Boolean.FALSE);
			}
			Collections.shuffle(flagVector);
			List<IVector> trimmedAlternatives = new ArrayList<>(dyadRankingLength);
			for (int i = 0; i < instance.getNumAttributes(); i++) {
				if (flagVector.get(i)) {
					trimmedAlternatives.add(instance.getLabel().get(i).getAlternative());
				}
			}
			SparseDyadRankingInstance trimmedDRInstance = new SparseDyadRankingInstance(instance.getLabel().get(0).getContext(), trimmedAlternatives);
			trimmedDataset.add(trimmedDRInstance);
		}
		return trimmedDataset;
	}

}

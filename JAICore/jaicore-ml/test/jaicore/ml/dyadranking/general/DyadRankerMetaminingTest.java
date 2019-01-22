package jaicore.ml.dyadranking.general;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.algorithm.ADyadRanker;
import jaicore.ml.dyadranking.algorithm.APLDyadRanker;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.algorithm.featuretransform.FeatureTransformPLDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;
import jaicore.ml.dyadranking.loss.DyadRankingLossUtil;
import jaicore.ml.dyadranking.loss.KendallsTauDyadRankingLoss;
import jaicore.ml.dyadranking.util.DyadStandardScaler;

/**
 * This is a test based on a dataset containing 400 dyad rankings of dataset and
 * pipeline metafeatures obtain from ML-Plan via landmarking and treemining.
 * 
 * @author Jonas Hanselle
 *
 */
@RunWith(Parameterized.class)
public class DyadRankerMetaminingTest {

	private static final String DATASET_FILE = "testsrc/ml/dyadranking/metamining-dataset.txt";

	// M = average ranking length
	private static final int M = 5;
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
		scaler.fit(trainData);
		scaler.transformInstances(trainData);
		DyadRankingDataset testData = new DyadRankingDataset(dataset.subList(N, dataset.size()));
		scaler.transformInstances(testData);
		System.out.println(trainData.size());
		System.out.println(testData.size());
		
		// trim dyad ranking instances for train data
//		trainData = randomlyTrimSparseDyadRankingInstances(trainData, M);

		try {

			// train the ranker
			ranker.train(trainData);
			double avgKendallTau = 0.0d;
			avgKendallTau = DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(), testData, ranker);
			System.out.println("Average Kendall's tau for " + ranker.getClass().getSimpleName() + ": " + avgKendallTau);
		} catch (TrainingException | PredictionException e) {
			e.printStackTrace();
		}

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
			if (drInstance.length() > dyadRankingLength) {
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
			} else {
				trimmedDataset.add(drInstance);
			}
		}
		return trimmedDataset;
	}

	@Parameters
	public static List<APLDyadRanker> supplyDyadRankers() {
		return Arrays.asList(new PLNetDyadRanker());
	}
}

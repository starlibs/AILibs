package jaicore.ml.dyadranking.activelearning;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
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
import jaicore.ml.dyadranking.activelearning.ActiveDyadRanker;
import jaicore.ml.dyadranking.activelearning.DyadScorePoolProvider;
import jaicore.ml.dyadranking.activelearning.PrototypicalPoolBasedActiveDyadRanker;
import jaicore.ml.dyadranking.algorithm.APLDyadRanker;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.loss.DyadRankingLossUtil;
import jaicore.ml.dyadranking.loss.KendallsTauDyadRankingLoss;

/**
 * @author Jonas Hanselle
 *
 */
@RunWith(Parameterized.class)
public class ActiveDyadRankingMetaminingTest {

	private static final String DYADS_SCORES_FILE = "testsrc/ml/dyadranking/meta-mining-dyads.txt";

	private static final double TRAIN_RATIO = 0.5d;

	private static boolean REMOVE_DYADS_WHEN_QUERIED = false;

	PLNetDyadRanker ranker;
	DyadRankingDataset dataset;

	public ActiveDyadRankingMetaminingTest(PLNetDyadRanker ranker) {
//		this.ranker = ranker;
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
		long seed = 23;
		SummaryStatistics[] results = new SummaryStatistics[100];
		for (int i = 0; i < results.length; i++) {
			results[i] = new SummaryStatistics();
		}

		ranker = new PLNetDyadRanker();
		DyadScorePoolProvider poolProvider = new DyadScorePoolProvider(dyadScorePairs);
		poolProvider.setRemoveDyadsWhenQueried(REMOVE_DYADS_WHEN_QUERIED);
		DyadRankingDataset dataset = new DyadRankingDataset();
		for (Vector vector : poolProvider.getInstanceFeatures()) {
			dataset.add(poolProvider.getDyadRankingInstanceForInstanceFeatures(vector));
		}

		Collections.shuffle(dataset, new Random(seed));

		DyadRankingDataset trainData = new DyadRankingDataset(dataset.subList(0, (int) (TRAIN_RATIO * dataset.size())));
		DyadRankingDataset testData = new DyadRankingDataset(
				dataset.subList((int) (TRAIN_RATIO * dataset.size()), dataset.size()));

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

		DyadRankingDataset queryAnswers = new DyadRankingDataset();
		for (IInstance instance : testData) {
			queryAnswers.add((IDyadRankingInstance) poolProvider.query(instance));
		}

		for (IInstance instance : testData) {
			IDyadRankingInstance drInstance = (IDyadRankingInstance) instance;

			poolProvider.removeDyadsFromPoolByInstances(drInstance.getDyadAtPosition(0).getInstance());
		}

		System.out.println("size after: " + poolProvider.getInstanceFeatures().size());

		ActiveDyadRanker activeDyadRanker = new PrototypicalPoolBasedActiveDyadRanker(ranker, poolProvider);

		System.out.println("tau of query: "
				+ DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(), testData, queryAnswers));

//		ActiveDyadRanker activeDyadRanker = new RandomPoolBasedActiveDyadRanker(ranker, poolProvider, seed);

		try {

			// train the ranker
//			for (IInstance inst : trainData)
//				System.out.println(((IDyadRankingInstance) inst).getDyadAtPosition(0).getInstance());
//			for (int i = 0; i < 100; i++) {
//				activeDyadRanker.activelyTrain(1);
//				double avgKendallTau = DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(),
//						testData, ranker);
//				results[i].addValue(avgKendallTau);
//				System.out.print(avgKendallTau + ",");
//			}
				ranker.train(trainData);
				double avgTauOutOfSample = DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(),
						testData, ranker);
				double avgTauInSample = DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(),
						trainData, ranker);
				System.out.println("out of sample: " + avgTauOutOfSample + "\t in sample:" + avgTauInSample);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		System.out.println("final averaged results: ");
//		for (int i = 0; i < results.length; i++) {
//			System.out.print(results[i].getMean() + ",");
//		}
	}

	@Parameters
	public static List<APLDyadRanker> supplyDyadRankers() {
		PLNetDyadRanker plNetRanker = new PLNetDyadRanker();
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

package jaicore.ml.dyadranking.activelearning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.dyadranking.algorithm.APLDyadRanker;
import jaicore.ml.dyadranking.algorithm.PLNetDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;
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

	private static final String GATSP_DATASET_FILE = "testsrc/ml/dyadranking/ga-tsp/GATSP-Data.txt";

	private static final int MAX_BATCH_SIZE = 5;
	private static final int TOP_RANKING_LENGTH = 5;
	private static final double RATIO_OF_OLD_SAMPLES_IN_MINIBATCH = 0.0d;

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
//		dataset = loadDatasetFromXXLAndCSV();
		dataset = new DyadRankingDataset();
		try {
			dataset.deserialize(new FileInputStream(new File(GATSP_DATASET_FILE)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void test() {
		System.out.println(ranker.getConfiguration());
		SummaryStatistics[] stats = new SummaryStatistics[100];
		for (int i = 0; i < stats.length; i++)
			stats[i] = new SummaryStatistics();
		int seed = 0;
//		for(int seed = 0; seed < 20; seed++) {
//		dataset = randomlyTrimSparseDyadRankingInstances(dataset, 20);

		Collections.shuffle(dataset, new Random(seed));

//		dataset = randomlyTrimSparseDyadRankingInstances(dataset, 5);

		// split data
		DyadRankingDataset trainData = new DyadRankingDataset(dataset.subList(0, N));
		DyadRankingDataset testData = new DyadRankingDataset(dataset.subList(N, dataset.size()));

		// standardize data
		DyadStandardScaler scaler = new DyadStandardScaler();
		scaler.fit(trainData);
		scaler.transformInstances(trainData);
		scaler.transformInstances(testData);

//		try {
//			ranker.train(new DyadRankingDataset( trainData.subList(0, 5)));
//		} catch (TrainingException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

		DyadDatasetPoolProvider poolProvider = new DyadDatasetPoolProvider(trainData);
		poolProvider.setRemoveDyadsWhenQueried(false);

// 		List<Vector> instFeat = new ArrayList<Vector>(poolProvider.getInstanceFeatures());
//		List<Dyad> dyads = new ArrayList<Dyad>(poolProvider.getDyadsByInstance(instFeat.get(0)));
//		List<Vector> alts1 = new ArrayList<Vector>();
//		List<Vector> alts2 = new ArrayList<Vector>();
//		alts1.add(dyads.get(1).getAlternative());
//		alts1.add(dyads.get(3).getAlternative());
//		alts2.add(dyads.get(5).getAlternative());
//		alts2.add(dyads.get(7).getAlternative());
//		
//		SparseDyadRankingInstance queryInstance = new SparseDyadRankingInstance(instFeat.get(0), alts1);
//		poolProvider.query(queryInstance);
//		SparseDyadRankingInstance queryInstance2 = new SparseDyadRankingInstance(instFeat.get(0), alts2);
//		SparseDyadRankingInstance queryInstance3 = new SparseDyadRankingInstance(instFeat.get(4), alts2);
//		
//		double[] instVals = instFeat.get(0).asArray();
//		double[] alt1Vals = alts2.get(0).asArray();
//		double[] alt2Vals = alts2.get(1).asArray();
//		List<Vector> newAlts = new ArrayList<Vector>();
//		newAlts.add(new DenseDoubleVector(alt1Vals));
//		newAlts.add(new DenseDoubleVector(alt2Vals));
//		
//		SparseDyadRankingInstance queryInstance4 = new SparseDyadRankingInstance(new DenseDoubleVector(instVals), newAlts);
//		
//		poolProvider.query(queryInstance);
//		poolProvider.query(queryInstance2);
//		poolProvider.query(queryInstance3);
//		poolProvider.query(queryInstance4);
//
//		System.out.println(queryInstance2.getDyadAtPosition(0) == queryInstance4.getDyadAtPosition(0));
//		System.out.println(queryInstance2.getDyadAtPosition(0).equals(queryInstance4.getDyadAtPosition(0)));
//		
//		for(Object obj : poolProvider.getQueriedRankings()) {
//			System.out.println(obj);
//		}

//		UCBPoolBasedActiveDyadRanker activeRanker = new UCBPoolBasedActiveDyadRanker(ranker, poolProvider, seed, 5, MAX_BATCH_SIZE);
			PrototypicalPoolBasedActiveDyadRanker activeRanker = new PrototypicalPoolBasedActiveDyadRanker(ranker,
					poolProvider, MAX_BATCH_SIZE, TOP_RANKING_LENGTH, RATIO_OF_OLD_SAMPLES_IN_MINIBATCH, 5, 5);
//		RandomPoolBasedActiveDyadRanker activeRanker = new RandomPoolBasedActiveDyadRanker(ranker, poolProvider, MAX_BATCH_SIZE, seed);

		try {

			// train the ranker
//			for(IInstance inst : trainData)
//				System.out.println(((IDyadRankingInstance) inst).getDyadAtPosition(0).getInstance());
			for (int i = 0; i < 100; i++) {
				activeRanker.activelyTrain(1);
				double avgKendallTau = DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(),
						testData, ranker);
				double avgKendallTauIS = DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(),
						new DyadRankingDataset(poolProvider.getQueriedRankings()), ranker);
//			System.out.print(avgKendallTau + ",");
				System.out.println("Current Kendalls Tau: " + avgKendallTau);
				System.out.println("Current Kendalls Tau IS: " + avgKendallTauIS);
//				System.out.println(poolProvider.getQueriedRankings());
//			stats[i].addValue(avgKendallTau);
			}

//			double avgKendallTau = 0.0d;
//			System.out.println("Average Kendall's tau for " + ranker.getClass().getSimpleName() + ": " + avgKendallTau);
//			assertTrue(avgKendallTau > 0.5d);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		}
		System.out.println("final results: ");
		for (int i = 0; i < stats.length; i++)
			System.out.print(stats[i].getMean() + ",");
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
		return Arrays.asList(plNetRanker);
	}
}

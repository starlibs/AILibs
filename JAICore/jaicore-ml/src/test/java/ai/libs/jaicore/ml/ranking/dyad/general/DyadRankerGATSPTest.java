package ai.libs.jaicore.ml.ranking.dyad.general;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

import ai.libs.jaicore.math.linearalgebra.DenseDoubleVector;
import ai.libs.jaicore.ml.ranking.dyad.DyadRankingLossUtil;
import ai.libs.jaicore.ml.ranking.dyad.dataset.DyadRankingDataset;
import ai.libs.jaicore.ml.ranking.dyad.dataset.SparseDyadRankingInstance;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.IPLDyadRanker;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.IPLNetDyadRankerConfiguration;
import ai.libs.jaicore.ml.ranking.dyad.learner.algorithm.PLNetDyadRanker;
import ai.libs.jaicore.ml.ranking.dyad.learner.util.AbstractDyadScaler;
import ai.libs.jaicore.ml.ranking.dyad.learner.util.DyadStandardScaler;
import ai.libs.jaicore.ml.ranking.loss.KendallsTauDyadRankingLoss;

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

	private static final String XXL_FILE = "testrsc/ml/dyadranking/ga-tsp/data_meta/GAMeta72-LR.txt";
	private static final String ALTERNATIVES_FEATURE_FILE = "testrsc/ml/dyadranking/ga-tsp/data_meta/GAMeta72-labeldescriptions.csv";
	private static final String ORDERINGS_FILE = "testrsc/ml/dyadranking/ga-tsp/data_meta/orderings.csv";

	// M = average ranking length
	private static final int M = 30;
	// N = number of training instances
	private static final int N = 120;
	// seed for shuffling the dataset
	private static final long SEED = 15;

	private PLNetDyadRanker ranker;
	private DyadRankingDataset dataset;

	public DyadRankerGATSPTest(final PLNetDyadRanker ranker) {
		this.ranker = ranker;
	}

	@Before
	public void init() throws IOException {
		// load dataset
		this.dataset = loadDatasetFromXXLAndCSV();
	}

	@Test
	public void test() throws PredictionException, InterruptedException, TrainingException {
		this.dataset = randomlyTrimSparseDyadRankingInstances(this.dataset, M);

		Collections.shuffle(this.dataset, new Random(SEED));

		// split data
		DyadRankingDataset trainData = new DyadRankingDataset(this.dataset.subList(0, N));
		DyadRankingDataset testData = new DyadRankingDataset(this.dataset.subList(N, this.dataset.size()));

		// trim dyad ranking instances for train data

		// standardize data
		AbstractDyadScaler scaler = new DyadStandardScaler();

		scaler.fit(trainData);
		scaler.transformInstances(trainData);
		scaler.transformInstances(testData);

		// train the ranker
		this.ranker.fit(trainData);
		double avgKendallTau = DyadRankingLossUtil.computeAverageLoss(new KendallsTauDyadRankingLoss(), testData, this.ranker);
		assertTrue(avgKendallTau > 0.5d);
	}

	/**
	 * Loads the dataset from the xxl and the csv files.
	 *
	 * @return {@link DyadRankingDataset} constructed of the instances and
	 *         alternatives in the corresponding files
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public static DyadRankingDataset loadDatasetFromXXLAndCSV() throws IOException {

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
		}

		List<IVector> alternativeFeatures = new ArrayList<>(100);

		// parse the file containing the features of the alternatives
		File alternativeFile = new File(ALTERNATIVES_FEATURE_FILE);
		try (BufferedReader reader = new BufferedReader(new FileReader(alternativeFile))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split(",");
				DenseDoubleVector vector = new DenseDoubleVector(tokens.length);
				for (int i = 0; i < vector.length(); i++) {
					vector.setValue(i, Double.parseDouble(tokens[i]));
				}
				alternativeFeatures.add(vector);
			}
		}


		// parse XXL file
		File xxlFile = new File(XXL_FILE);
		int numAttributes = 0;
		int numLabels = 0;
		try (BufferedReader reader = new BufferedReader(new FileReader(xxlFile))) {
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
				default:

				}
			}

			// skip two lines
			reader.readLine();
			reader.readLine();

			List<IVector> instanceFeatures = new ArrayList<>(246);
			List<ArrayList<IVector>> alternativesList = new ArrayList<>(246);
			int lineIndex = 0;
			while ((line = reader.readLine()) != null) {
				tokens = line.split("\t");
				IVector instance = new DenseDoubleVector(numAttributes);
				ArrayList<IVector> alternatives = new ArrayList<>(numLabels);

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
				if (flagVector.get(i).booleanValue()) {
					trimmedAlternatives.add(instance.getLabel().get(i).getAlternative());
				}
			}
			SparseDyadRankingInstance trimmedDRInstance = new SparseDyadRankingInstance(instance.getLabel().get(0).getContext(), trimmedAlternatives);
			trimmedDataset.add(trimmedDRInstance);
		}
		return trimmedDataset;
	}

	@Parameters
	public static List<IPLDyadRanker> supplyDyadRankers() {
		PLNetDyadRanker plNetRanker = new PLNetDyadRanker();
		// Use a simple config such that the test finishes quickly
		plNetRanker.getConfig().put(IPLNetDyadRankerConfiguration.K_ACTIVATION_FUNCTION, "SIGMOID");
		plNetRanker.getConfig().put(IPLNetDyadRankerConfiguration.K_PLNET_HIDDEN_NODES, "5");
		plNetRanker.getConfig().put(IPLNetDyadRankerConfiguration.K_MAX_EPOCHS, "100");
		plNetRanker.getConfig().put(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_INTERVAL, "1");
		plNetRanker.getConfig().put(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_PATIENCE, "5");
		plNetRanker.getConfig().put(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_RETRAIN, "false");
		plNetRanker.getConfig().put(IPLNetDyadRankerConfiguration.K_PLNET_LEARNINGRATE, "0.1");
		plNetRanker.getConfig().put(IPLNetDyadRankerConfiguration.K_MINI_BATCH_SIZE, "1");
		plNetRanker.getConfig().put(IPLNetDyadRankerConfiguration.K_PLNET_SEED, Long.toString(SEED));
		plNetRanker.getConfig().put(IPLNetDyadRankerConfiguration.K_EARLY_STOPPING_TRAIN_RATIO, "0.8");
		return Arrays.asList(plNetRanker);
	}
}

package jaicore.ml.dyadranking.general;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.dyadranking.algorithm.ADyadRanker;
import jaicore.ml.dyadranking.algorithm.FeatureTransformPLDyadRanker;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.dataset.SparseDyadRankingInstance;

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
//@RunWith(Parameterized.class)
public class DyadRankerGATSPTest {

	private static final String XXL_FILE = "testsrc/ml/dyadranking/ga-tsp/data_meta/GAMeta72-LR.txt";
	private static final String ALTERNATIVES_FEATURE_FILE = "testsrc/ml/dyadranking/ga-tsp/data_meta/GAMeta72-labeldescriptions.csv";

	// M = average ranking length
	private static final int M = 5;
	// N = number of training instances
	private static final int N = 30;
	// seed for shuffling the dataset
	private static final long seed = 15;

	ADyadRanker ranker;
	DyadRankingDataset dataset;

	@Before
	public void init() {
		// load dataset
		dataset = loadDatasetFromXXLAndCSV(XXL_FILE, ALTERNATIVES_FEATURE_FILE);
	}

	@Test
	public void test() {

		Collections.shuffle(dataset, new Random(seed));

		// split data
		DyadRankingDataset trainData = (DyadRankingDataset) dataset.subList(0, N);
		DyadRankingDataset testData = (DyadRankingDataset) dataset.subList(N, dataset.size());

		// tirim dyad ranking instances for train data
		trainData = randomlyTrimSparseDyadRankingInstances(trainData, M);

		try {

			// train the ranker
			ranker.train(trainData);
			List<IDyadRankingInstance> predictions = ranker.predict(testData);

			// compute average rank correlation
			DescriptiveStatistics rankCorrelationStats = new DescriptiveStatistics();
			for (IDyadRankingInstance prediction : predictions) {
				KendallsCorrelation kendallsCorrelation = new KendallsCorrelation();
				// TODO computation of loss				
			}

		} catch (TrainingException | PredictionException e) {
			e.printStackTrace();
		}

	}

	@Parameters
	public static List<ADyadRanker> supplyParams() {
		return Arrays.asList(new FeatureTransformPLDyadRanker());
	}

	/**
	 * Loads the dataset from an xxl and a csv file.
	 * 
	 * @param filePathXXL                 xxl file containing the label ranking
	 * @param filePathAlternativeFeatures csv file containing the features for the
	 *                                    alternatives (labels)
	 * @return {@link DyadRankingDataset} constructed of the instances and
	 *         alternatives in the corresponding files
	 */
	private static DyadRankingDataset loadDatasetFromXXLAndCSV(String filePathXXL, String filePathAlternativeFeatures) {

		DyadRankingDataset dataset = new DyadRankingDataset();

		List<Vector> alternativeFeatures = new ArrayList<Vector>(100);

		// parse the file containing the features of the alternatives
		File alternativeFile = new File(filePathAlternativeFeatures);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(alternativeFile));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split(",");
				DenseDoubleVector vector = new DenseDoubleVector(tokens.length);
				for (int i = 0; i < vector.length(); i++) {
					vector.setValue(i, Double.parseDouble(tokens[i]));
				}
				alternativeFeatures.add(vector);
			}
			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(alternativeFeatures.size());

		// parse XXL file
		File xxlFile = new File(filePathXXL);
		int numAttributes = 0;
		int numLabels = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(xxlFile));
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
				}
			}
			System.out.println("numAttributes: " + numAttributes);
			System.out.println("numLabels: " + numLabels);

			// skip two lines
			reader.readLine();
			reader.readLine();

			while ((line = reader.readLine()) != null) {
				tokens = line.split("\t");

				Vector instance = new DenseDoubleVector(numAttributes);
				List<Vector> alternatives = new ArrayList<Vector>(numLabels);

				// add the instances to the dyad ranking instance
				for (int i = 0; i < numAttributes; i++) {
					instance.setValue(i, Double.parseDouble(tokens[i]));
				}

				// add the alternatives to the dyad ranking instance
				for (int i = numAttributes; i < tokens.length; i++) {
					int index = Integer.parseInt(tokens[i]) - 1;
					alternatives.add(alternativeFeatures.get(index));
				}
				SparseDyadRankingInstance drInstance = new SparseDyadRankingInstance(instance, alternatives);
				dataset.add(drInstance);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	private static DyadRankingDataset randomlyTrimSparseDyadRankingInstances(DyadRankingDataset dataset,
			int dyadRankingLength) {
		DyadRankingDataset trimmedDataset = new DyadRankingDataset();
		for (IInstance instance : dataset) {
			SparseDyadRankingInstance drInstance = (SparseDyadRankingInstance) instance;
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
			System.out.println("original: " + drInstance);
			System.out.println("trimmed: " + trimmedDRInstance + "\n\n");
		}
		return trimmedDataset;
	}
}

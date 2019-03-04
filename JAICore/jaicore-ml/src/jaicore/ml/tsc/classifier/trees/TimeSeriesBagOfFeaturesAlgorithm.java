package jaicore.ml.tsc.classifier.trees;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.classifier.ASimplifiedTSCAlgorithm;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.features.TimeSeriesFeature;
import jaicore.ml.tsc.util.MathUtil;
import jaicore.ml.tsc.util.TimeSeriesUtil;
import jaicore.ml.tsc.util.WekaUtil;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

public class TimeSeriesBagOfFeaturesAlgorithm
		extends ASimplifiedTSCAlgorithm<Integer, TimeSeriesBagOfFeaturesClassifier> {

	/**
	 * Log4j logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesBagOfFeaturesAlgorithm.class);

	/**
	 * Seed used for all randomized operations.
	 */
	private int seed;

	/**
	 * See {@link IAlgorithm#getNumCPUs()}.
	 */
	private int cpus = 1;

	/**
	 * See {@link IAlgorithm#getTimeout()}.
	 */
	private TimeOut timeout = new TimeOut(Integer.MAX_VALUE, TimeUnit.SECONDS);

	public TimeSeriesBagOfFeaturesAlgorithm(final int seed) {
		// TODO Auto-generated constructor stub
		this.seed = seed;
	}

	@Override
	public TimeSeriesBagOfFeaturesClassifier call()
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		// TODO Training procedure

		TimeSeriesDataset dataset = this.getInput();
		if (dataset == null || dataset.isEmpty())
			throw new IllegalArgumentException("Dataset used for training must not be null or empty!");

		if (dataset.isMultivariate())
			LOGGER.info(
					"Only univariate data is used for training (matrix index 0), although multivariate data is available.");

		// Shuffle instances?
		TimeSeriesUtil.shuffleTimeSeriesDataset(dataset, this.seed);

		double[][] data = dataset.getValuesOrNull(0);
		int[] targets = dataset.getTargets();
		
		if(data == null || data.length == 0 || targets == null || targets.length == 0)
			throw new IllegalArgumentException(
					"The given dataset for training must not contain a null or empty data or target matrix.");

		// Get number classes
		int C = TimeSeriesUtil.getNumberOfClasses(dataset);

		// Standardize each time series to zero mean and unit standard deviation (z
		// transformation)
		// TODO: Use unifying implementation
		for (int i = 0; i < dataset.getNumberOfInstances(); i++) {
			data[i] = TimeSeriesUtil.zNormalize(data[i], true);
		}

		// TODO Subsequences and feature extraction
		int T = dataset.getNumberOfVariables(); // Time series length
		double zProp = 0.5d;
		int lMin = (int) (zProp * T);

		int wMin = 3; // Minimum interval length used for meaningful intervals

		int d = (int) Math.floor(lMin / wMin); // Number of intervals for each subsequence

		int r = (int) Math.floor(T / wMin); // Number of possible intervals in a time series

		int numBins = 10; // Number of bins used for the CPEs
		int numFolds = 10;

		// TODO Generate r-d subsequences with each d intervals and calculate features
		int[][][] subsequences = new int[r - d][d][2];
		Random random = new Random(seed);
		for (int i = 0; i < r - d; i++) {

			for (int j = 0; j < d; j++) {
				int startIndex = random.nextInt(T - lMin);
				int subSeqLength = random.nextInt(T - lMin - startIndex);
				subsequences[i][j][0] = startIndex;
				subsequences[i][j][1] = startIndex + subSeqLength; // exclusive
			}
		}

		// Generate features
		double[][][][] generatedFeatures = new double[data.length][r - d][d][3];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < r - d; j++) {
				for (int k = 0; k < d; k++) {
					generatedFeatures[i][j][k] = TimeSeriesFeature.getFeatures(data[i], subsequences[j][k][0],
							subsequences[j][k][1] - 1, true);
				}
			}
		}

		// TODO Generate class probability estimate (CPE) for each instance using a
		// classifier

		double[][] subSeqValueMatrix = new double[(r - d) * data.length][d * 3];
		int[] targetMatrix = new int[(r - d) * data.length];

		for (int i = 0; i < r - d; i++) {
			
			for (int j = 0; j < data.length; j++) {
				double[] intervalFeatures = new double[d*3];
				for(int k=0; k<d; k++) {
					intervalFeatures[k * d] = generatedFeatures[j][i][k][0];
					intervalFeatures[k * d + 1] = generatedFeatures[j][i][k][1]; 
					intervalFeatures[k * d + 2] = generatedFeatures[j][i][k][2]; 
				}
				subSeqValueMatrix[i * data.length + j] = intervalFeatures;
				
				targetMatrix[i * data.length + j] = targets[j];
			}
		}

		double[][] probs = new double[(r - d) * data.length][C];
		int numTestInstsPerFold = probs.length / numFolds;

		// rf.measureOutOfBagError()
		// TODO: Measure OOB probabilities
		for (int i = 0; i < numFolds; i++) {
			RandomForest rf = new RandomForest();

			double[][] trainingValueMatrix = new double[(numFolds - 1) * numTestInstsPerFold][C];
			if (i == 0) {
				System.arraycopy(subSeqValueMatrix, numTestInstsPerFold, trainingValueMatrix, 0,
						(numFolds - 1) * numTestInstsPerFold);
			} else if (i == numFolds - 1) {
				System.arraycopy(subSeqValueMatrix, 0, trainingValueMatrix, 0, (numFolds - 1) * numTestInstsPerFold);
			} else {
				System.arraycopy(subSeqValueMatrix, 0, trainingValueMatrix, 0, i * numTestInstsPerFold);
				System.arraycopy(subSeqValueMatrix, (i + 1) * numTestInstsPerFold, trainingValueMatrix,
						i * numTestInstsPerFold, (numFolds - i - 1) * numTestInstsPerFold);
			}

			ArrayList<double[][]> valueMatrices = new ArrayList<>();
			valueMatrices.add(trainingValueMatrix);
			TimeSeriesDataset trainingDS = new TimeSeriesDataset(valueMatrices);

			try {
				WekaUtil.buildWekaClassifierFromSimplifiedTS(rf, trainingDS);
			} catch (TrainingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Store probabilities
			double[][] currTestMatrix = new double[numTestInstsPerFold][C];
			System.arraycopy(subSeqValueMatrix, i * numTestInstsPerFold, currTestMatrix, 0, numTestInstsPerFold);
			int[] currTestTargetMatrix = new int[numTestInstsPerFold];
			System.arraycopy(targetMatrix, i * numTestInstsPerFold, currTestTargetMatrix, 0, numTestInstsPerFold);
			
			ArrayList<double[][]> testValueMatrices = new ArrayList<>();
			testValueMatrices.add(currTestMatrix);
			TimeSeriesDataset testDataset = new TimeSeriesDataset(testValueMatrices, currTestTargetMatrix);
			Instances testInstances = WekaUtil.simplifiedTimeSeriesDatasetToWekaInstances(testDataset);
			double[][] testProbs = null;
			try {
				testProbs = rf.distributionsForInstances(testInstances);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int j = 0; j < testProbs.length; j++) {
				probs[i * numTestInstsPerFold + j] = testProbs[j];
			}
		}

		// TODO: Discretize probability and form histogram
		int[][] discretizedProbs = discretizeProbs(numBins, probs);

		// TODO: Form histogram
		Pair<int[][][], int[][]> histFreqPair = formHistogramsAndRelativeFreqs(discretizedProbs, targets, data.length,
				C, numBins);
		int[][][] histograms = histFreqPair.getX();
		int[][] relativeFrequencies = histFreqPair.getY();

		// TODO: Build final classifier
		double[][] finalInstances = generateHistogramInstances(histograms, relativeFrequencies);
		ArrayList<double[][]> finalMatrices = new ArrayList<>();
		finalMatrices.add(finalInstances);

		TimeSeriesDataset finalDataset = new TimeSeriesDataset(finalMatrices, targets);
		RandomForest finalRf = new RandomForest();
		try {
			WekaUtil.buildWekaClassifierFromSimplifiedTS(finalRf, finalDataset);
		} catch (TrainingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static double[][] generateHistogramInstances(final int[][][] histograms,
			final int[][] relativeFreqsOfClasses) {
		int featureLength = histograms[0].length * histograms[1].length + relativeFreqsOfClasses[0].length;
		final double[][] results = new double[histograms.length][featureLength];

		for (int i = 0; i < results.length; i++) {
			double[] instFeatures = new double[featureLength];
			int featureIdx = 0;
			for (int j = 0; j < histograms[i].length; j++) {
				for (int k = 0; k < histograms[i][j].length; k++) {
					instFeatures[featureIdx++] = histograms[i][j][k];
				}
			}

			for (int j = 0; j < relativeFreqsOfClasses[i].length; j++) {
				instFeatures[featureIdx++] = relativeFreqsOfClasses[i][j];
			}
		}

		return results;
	}

	public static Pair<int[][][], int[][]> formHistogramsAndRelativeFreqs(final int[][] discretizedProbs,
			final int[] targets, final int numInstances,
			final int numClasses,
			final int numBins) {
		final int[][][] histograms = new int[numInstances][numClasses][numBins];
		final int[][] relativeFrequencies = new int[numInstances][numClasses];

		for (int i = 0; i < discretizedProbs.length; i++) {
			// Index of the instance
			int instanceIdx = (int) (i / numInstances);
			// int instanceClass = targets[instanceIdx];
			for (int c = 0; c < numClasses; c++) {
				int bin = discretizedProbs[i][c];
				histograms[instanceIdx][c][bin]++;
			}
			
			int predClass = MathUtil.argmax(discretizedProbs[i]);
			relativeFrequencies[instanceIdx][predClass]++;
		}

		return new Pair<int[][][], int[][]>(histograms, relativeFrequencies);
	}

	public static int[][] discretizeProbs(final int numBins, final double[][] probs) {
		int[][] results = new int[probs.length][probs[0].length];
		
		final double steps = 1 / numBins;

		for (int i = 0; i < results.length; i++) {
			int[] discretizedProbs = new int[probs[i].length];
			for (int j = 0; j < discretizedProbs.length; j++) {
				discretizedProbs[j] = (int) (probs[i][j] / steps);
			}
			results[i] = discretizedProbs;
		}

		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerListener(Object listener) {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNumCPUs() {
		return this.cpus;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNumCPUs(int numberOfCPUs) {
		this.cpus = numberOfCPUs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTimeout(long timeout, TimeUnit timeUnit) {
		this.timeout = new TimeOut(timeout, timeUnit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTimeout(TimeOut timeout) {
		this.timeout = timeout;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TimeOut getTimeout() {
		return this.timeout;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AlgorithmEvent nextWithException()
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAlgorithmConfig getConfig() {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<AlgorithmEvent> iterator() {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AlgorithmEvent next() {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cancel() {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

}

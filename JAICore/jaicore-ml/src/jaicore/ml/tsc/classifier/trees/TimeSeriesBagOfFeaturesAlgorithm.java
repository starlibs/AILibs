package jaicore.ml.tsc.classifier.trees;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

/**
 * Algorithm to train a Time Series Bag-of-Features (TSBF) classifier as
 * described in Baydogan, Mustafa & Runger, George & Tuv, Eugene. (2013). A
 * Bag-of-Features Framework to Classify Time Series. IEEE Transactions on
 * Pattern Analysis and Machine Intelligence. 35. 2796-802.
 * 10.1109/TPAMI.2013.72.
 * 
 * @author Julian Lienen
 *
 */
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
	 * Number of bins used for the CPEs.
	 */
	private int numBins;

	/**
	 * Number of folds used for the OOB probability estimation in the training
	 * phase.
	 */
	private int numFolds;

	/**
	 * See {@link IAlgorithm#getNumCPUs()}.
	 */
	private int cpus = 1;

	/**
	 * See {@link IAlgorithm#getTimeout()}.
	 */
	private TimeOut timeout = new TimeOut(Integer.MAX_VALUE, TimeUnit.SECONDS);

	/**
	 * Indicator whether Bessel's correction should in feature generation.
	 */
	public static final boolean USE_BIAS_CORRECTION = false;

	/**
	 * Proportion of the total time series length to be used for the subseries
	 * generation.
	 */
	private double zProp;

	/**
	 * The minimal interval length used for the interval generation.
	 */
	private int minIntervalLength;

	/**
	 * Indicator whether the z transformation should be used for the instances at
	 * training and prediction time.
	 */
	private boolean useZNormalization;

	/**
	 * Number of trees used in the internal Random Forest classifier.
	 */
	private static final int NUM_TREES_IN_FOREST = 500;

	/**
	 * Constructor for a TSBF training algorithm.
	 * 
	 * @param seed
	 *            Seed used for randomized operations
	 * @param numBins
	 *            Number of bins used for the histogram generation
	 * @param numFolds
	 *            Number of folds for the internal OOB probability CV estimation
	 * @param zProp
	 *            Proportion of the total time series length to be used for the
	 *            subseries generation
	 * @param minIntervalLength
	 *            The minimal interval length used for the interval generation
	 * @param useZNormalization
	 *            Indicator whether the Z normalization should be used
	 */
	public TimeSeriesBagOfFeaturesAlgorithm(final int seed, final int numBins, final int numFolds, final double zProp,
			final int minIntervalLength, final boolean useZNormalization) {
		this.seed = seed;
		this.numBins = numBins;
		this.numFolds = numFolds;

		if (this.zProp > 1)
			this.zProp = 1d;
		else if (this.zProp < 0d)
			throw new IllegalArgumentException("Parameter zProp must be higher than 0!");
		else
			this.zProp = zProp;
		
		this.minIntervalLength = minIntervalLength;
		this.useZNormalization = useZNormalization;
	}

	/**
	 * Training procedure construction a Time Series Bag-of-Features (TSBF)
	 * classifier using the given input data.
	 */
	@Override
	public TimeSeriesBagOfFeaturesClassifier call()
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		// Training procedure

		TimeSeriesDataset dataset = this.getInput();
		if (dataset == null || dataset.isEmpty())
			throw new IllegalArgumentException("Dataset used for training must not be null or empty!");

		if (dataset.isMultivariate())
			LOGGER.info(
					"Only univariate data is used for training (matrix index 0), although multivariate data is available.");

		// Shuffle instances
		TimeSeriesUtil.shuffleTimeSeriesDataset(dataset, this.seed);

		double[][] data = dataset.getValuesOrNull(0);
		int[] targets = dataset.getTargets();

		if (data == null || data.length == 0 || targets == null || targets.length == 0)
			throw new IllegalArgumentException(
					"The given dataset for training must not contain a null or empty data or target matrix.");

		// Get number classes
		int C = TimeSeriesUtil.getNumberOfClasses(dataset);

		// Standardize each time series to zero mean and unit standard deviation (z
		// transformation)
		// TODO: Use unifying implementation
		if (this.useZNormalization) {
			for (int i = 0; i < dataset.getNumberOfInstances(); i++) {
				data[i] = TimeSeriesUtil.zNormalize(data[i], true);
			}
		}

		// Specify parameters used for subsequence and interval generation
		int T = data[0].length; // Time series length
		int lMin = (int) (this.zProp * T); // Minimum subsequence length

		// Check lower bound for minimum subsequence length
		if (lMin < minIntervalLength)
			lMin = minIntervalLength;

		// Check upper bound for minimum subsequence length
		if (lMin >= T - this.minIntervalLength)
			lMin -= this.minIntervalLength;

		// Number of intervals for each subsequence
		int d = this.getD(lMin);

		// Number of possible intervals in a time series
		int r = this.getR(T);

		// Generate r-d subsequences with each d intervals and calculate features
		Pair<int[][], int[][][]> subSeqIntervals = generateSubsequencesAndIntervals(r, d, lMin, T);
		int[][] subsequences = subSeqIntervals.getX();
		int[][][] intervals = subSeqIntervals.getY();

		// Generate features
		double[][][][] generatedFeatures = generateFeatures(data, subsequences, intervals);

		// Generate class probability estimate (CPE) for each instance using a
		// classifier
		int numFeatures = (d + 1) * 3 + 2;
		double[][] subSeqValueMatrix = new double[(r - d) * data.length][numFeatures];
		int[] targetMatrix = new int[(r - d) * data.length];

		for (int i = 0; i < r - d; i++) {

			for (int j = 0; j < data.length; j++) {
				double[] intervalFeatures = new double[numFeatures];
				for (int k = 0; k < d + 1; k++) {
					intervalFeatures[k * 3] = generatedFeatures[j][i][k][0];
					intervalFeatures[k * 3 + 1] = generatedFeatures[j][i][k][1];
					intervalFeatures[k * 3 + 2] = generatedFeatures[j][i][k][2];
				}
				intervalFeatures[intervalFeatures.length - 2] = subsequences[i][0];
				intervalFeatures[intervalFeatures.length - 1] = subsequences[i][1];

				subSeqValueMatrix[j * (r-d) + i] = intervalFeatures;

				targetMatrix[j * (r-d) + i] = targets[j];
			}
		}

		// Measure OOB probabilities
		RandomForest subseriesClf = new RandomForest();
		subseriesClf.setNumIterations(NUM_TREES_IN_FOREST);
		double[][] probs = null;
		try {
			probs = measureOOBProbabilitiesUsingCV(subSeqValueMatrix, targetMatrix, (r - d) * data.length, numFolds, C, subseriesClf);
		} catch (TrainingException e1) {
			throw new AlgorithmException(e1, "Could not measure OOB probabilities using CV.");
		}

		// Train final subseries classifier
		try {
			WekaUtil.buildWekaClassifierFromSimplifiedTS(subseriesClf,
					TimeSeriesUtil.createDatasetForMatrix(targetMatrix, subSeqValueMatrix));
		} catch (TrainingException e) {
			throw new AlgorithmException(e,
					"Could not train the sub series Random Forest classifier due to an internal Weka exception.");
		}

		// Discretize probability and form histogram
		int[][] discretizedProbs = discretizeProbs(numBins, probs);
		Pair<int[][][], int[][]> histFreqPair = formHistogramsAndRelativeFreqs(discretizedProbs, targets, data.length,
				C, numBins);
		int[][][] histograms = histFreqPair.getX();
		int[][] relativeFrequencies = histFreqPair.getY();

		// Build final classifier
		double[][] finalInstances = generateHistogramInstances(histograms, relativeFrequencies);
		RandomForest finalClf = new RandomForest();
		finalClf.setNumIterations(NUM_TREES_IN_FOREST);
		try {
			WekaUtil.buildWekaClassifierFromSimplifiedTS(finalClf,
					TimeSeriesUtil.createDatasetForMatrix(targets, finalInstances));
		} catch (TrainingException e) {
			throw new AlgorithmException(e,
					"Could not train the final Random Forest classifier due to an internal Weka exception.");
		}

		// Update model
		this.model.setSubseriesClf(subseriesClf);
		this.model.setFinalClf(finalClf);
		this.model.setNumClasses(C);
		this.model.setIntervals(intervals);
		this.model.setSubsequences(subsequences);
		this.model.setTrained(true);

		return this.model;
	}

	/**
	 * Method randomly determining the subsequences and their intervals to be used
	 * for feature generation of the instances. As a result, a pair of each
	 * subsequence's start and end index and the intervals' start and end indices is
	 * returned.
	 * 
	 * @param r
	 *            The number of possible intervals in a time series
	 * @param d
	 *            The number of intervals for each subsequence
	 * @param lMin
	 *            The minimum subsequence length
	 * @param T
	 *            The length of the time series
	 * @return a pair of each subsequence's start and end index and the intervals'
	 *         start and end indices
	 */
	public Pair<int[][], int[][][]> generateSubsequencesAndIntervals(final int r, final int d, final int lMin,
			final int T) {
		int[][] subsequences = new int[r - d][2];
		int[][][] intervals = new int[r - d][d][2];

		Random random = new Random(seed);
		for (int i = 0; i < r - d; i++) {
			int startIndex = random.nextInt(T - lMin);
			int subSeqLength = random.nextInt(T - lMin - startIndex) + lMin;

			// Store subseries borders (also used for feature generation)
			subsequences[i][0] = startIndex;
			subsequences[i][1] = startIndex + subSeqLength + 1; // exclusive

			int intervalLength = (int) ((double) (subsequences[i][1] - subsequences[i][0]) / ((double) d));
			if (intervalLength < this.minIntervalLength)
				throw new IllegalStateException(
						"The induced interval length must not be lower than the minimum interval length!");

			if (intervalLength > this.minIntervalLength) {
				// Select random length for interval
				intervalLength = random.nextInt(intervalLength - this.minIntervalLength + 1) + this.minIntervalLength;
			}

			for (int j = 0; j < d; j++) {
				intervals[i][j][0] = subsequences[i][0] + j * intervalLength;
				intervals[i][j][1] = subsequences[i][0] + (j + 1) * intervalLength; // exclusive
			}
		}
		return new Pair<int[][], int[][][]>(subsequences, intervals);
	}

	/**
	 * Function generating the features for the internal probability measurement
	 * model based on the given <code>subseries</code> and their corresponding
	 * </code>intervals</code>. The features are built using the
	 * {@link TimeSeriesFeature} implementation. As a result, a tensor consisting of
	 * the generated features for each interval in each subsequence for each
	 * instance is returned (4 dimensions).
	 * 
	 * @param data
	 *            The data used for feature generation
	 * @param subsequences
	 *            The subsequences used for feature generation (the start and end
	 *            [exclusive] index is stored for each subsequence)
	 * @param intervals
	 *            The intervals of each subsequence used for the feature generation
	 *            (the start and end [exclusive] index is stored for each interval)
	 * @return Returns a tensor consisting of the generated features for each
	 *         interval in each subsequence for each instance
	 */
	public static double[][][][] generateFeatures(final double[][] data, final int[][] subsequences,
			final int[][][] intervals) {

		double[][][][] generatedFeatures = new double[data.length][subsequences.length][intervals[0].length
				+ 1][TimeSeriesFeature.NUM_FEATURE_TYPES];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < subsequences.length; j++) {
				for (int k = 0; k < intervals[j].length; k++) {
					generatedFeatures[i][j][k] = TimeSeriesFeature.getFeatures(data[i], intervals[j][k][0],
							intervals[j][k][1] - 1, USE_BIAS_CORRECTION);
					generatedFeatures[i][j][k][1] *= generatedFeatures[i][j][k][1];
				}
				generatedFeatures[i][j][intervals[j].length] = TimeSeriesFeature.getFeatures(data[i],
						subsequences[j][0],
						subsequences[j][1] - 1,
						USE_BIAS_CORRECTION);
				generatedFeatures[i][j][intervals[j].length][1] *= generatedFeatures[i][j][intervals[j].length][1];
			}
		}
		return generatedFeatures;
	}

	/**
	 * Method calculating the number of intervals for each subsequence.
	 * 
	 * @param lMin
	 *            The minimum subsequence length
	 * @return Returns the number of intervals for each subsequence
	 */
	private int getD(final int lMin) {
		return lMin > this.minIntervalLength ? (int) Math.floor((double) lMin / (double) this.minIntervalLength) : 1;
	}

	/**
	 * Method returning the number of possible intervals in the time series used for
	 * subsequences and intervals generation.
	 * 
	 * @param T
	 *            The length of the time series
	 * @return Returns the number of possible intervals in the time series
	 */
	private int getR(final int T) {
		return (int) Math.floor((double) T / (double) this.minIntervalLength);
	}

	/**
	 * Generates a matrix consisting of the histogram values for each instance out
	 * of the given <code>histograms</code> and the relative frequencies of classes
	 * for each instance. The histogram values for each instance, class and bin are
	 * concatenated. Furthermore, the relative frequencies are also added to the
	 * instance's features.
	 * 
	 * @param histograms
	 *            The histograms for each instance (number of instances x number of
	 *            classes - 1 x number of bins)
	 * @param relativeFreqsOfClasses
	 *            The relative frequencies of the classes for each instance
	 *            (previously extracted from each subseries instance per origin
	 *            instance; dimensionality is number of instances x number of
	 *            classes)
	 * @return Returns a matrix storing the features for each instance (number of
	 *         instances x number of features)
	 */
	public static double[][] generateHistogramInstances(final int[][][] histograms,
			final int[][] relativeFreqsOfClasses) {
		int featureLength = histograms[0].length * histograms[0][0].length + relativeFreqsOfClasses[0].length;
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

			results[i] = instFeatures;
		}

		return results;
	}

	/**
	 * Function measuring the out-of-bag (OOB) probabilities using a cross
	 * validation with <code>numFolds</code> many folds. For each fold, the data
	 * given by <code>subSeqValueMatrix</code> is split into a training and test
	 * set. The test set's probabilities are then derived by a trained Random Forest
	 * classifier.
	 * 
	 * @param subSeqValueMatrix
	 *            Input data used to derive the OOB probabilities
	 * @param targetMatrix
	 *            The target values of the input data
	 * @param numProbInstances
	 *            Number of instances for which the probabilities should be derived
	 * @param numFolds
	 *            Number of folds used for the measurement
	 * @param numClasses
	 *            Number of total classes
	 * @param rf
	 *            Random Forest classifier which is retrained in each fold
	 * @return Returns a matrix storing the probability for each input instance
	 *         given by <code>subSeqValueMatrix</code>
	 * @throws TrainingException
	 *             Thrown when the classifier <code>rf</code> could not be trained
	 *             in any fold
	 */
	public static double[][] measureOOBProbabilitiesUsingCV(final double[][] subSeqValueMatrix,
			final int[] targetMatrix, final int numProbInstances, final int numFolds, final int numClasses,
			final RandomForest rf)
			throws TrainingException {

		double[][] probs = new double[numProbInstances][numClasses];
		int numTestInstsPerFold = (int) ((double) probs.length / (double) numFolds);

		for (int i = 0; i < numFolds; i++) {
			// Generate training instances for fold
			Pair<TimeSeriesDataset, TimeSeriesDataset> trainingTestDatasets = TimeSeriesUtil
					.getTrainingAndTestDataForFold(i, numFolds, subSeqValueMatrix, targetMatrix);
			TimeSeriesDataset trainingDS = trainingTestDatasets.getX();

			WekaUtil.buildWekaClassifierFromSimplifiedTS(rf, trainingDS);

			// Prepare test instances
			TimeSeriesDataset testDataset = trainingTestDatasets.getY();
			Instances testInstances = WekaUtil.simplifiedTimeSeriesDatasetToWekaInstances(testDataset, IntStream
					.rangeClosed(0, numClasses - 1).boxed().map(v -> String.valueOf(v)).collect(Collectors.toList()));

			double[][] testProbs = null;
			try {
				testProbs = rf.distributionsForInstances(testInstances);
			} catch (Exception e) {
				String errorMessage = "Could not induce test probabilities in OOB probability estimation due to an internal Weka error.";
				LOGGER.error(errorMessage, e);
				throw new TrainingException(errorMessage, e);
			}

			// Store induced probabilities
			for (int j = 0; j < testProbs.length; j++) {
				probs[i * numTestInstsPerFold + j] = testProbs[j];
			}
		}

		return probs;
	}

	/**
	 * Function calculating the histograms as described in the paper's section 2.2
	 * ("Codebook and Learning"). All probabilities rows belonging to one instance
	 * are aggregated by evaluating the discretized probabilities
	 * <code>discretizedProbs</code>. Furthermore, the relative frequencies of the
	 * classes are collected. As the result, a pair of the generated histograms for
	 * all instances and the corresponding normalized relative class frequencies is
	 * returned.
	 * 
	 * @param discretizedProbs
	 *            The discretized (binned) probabilities of all instance's subseries
	 *            rows (the number of rows must be divisible by the number of total
	 *            instances)
	 * @param targets
	 *            The targets corresponding to the discretized probabilities
	 * @param numInstances
	 *            The total number of instances (must be <= the number of rows in
	 *            <code>discretizedProbs</code>
	 * @param numClasses
	 *            The total number of classes
	 * @param numBins
	 *            The number of bins using within the discretization
	 * @return Returns a pair of the histograms per instance
	 *         (<code>numInstances</code> in total) and the corresponding relative
	 *         frequencies (normalized)
	 */
	public static Pair<int[][][], int[][]> formHistogramsAndRelativeFreqs(final int[][] discretizedProbs,
			final int[] targets, final int numInstances, final int numClasses, final int numBins) {

		if (discretizedProbs.length < numInstances)
			throw new IllegalArgumentException(
					"The number of discretized probabilities must not be lower than the number of instances!");
		if (discretizedProbs.length % numInstances != 0)
			throw new IllegalArgumentException(
					"The number of discretized probabilities must be divisible by the number of instances!");

		final int[][][] histograms = new int[numInstances][numClasses - 1][numBins];
		final int[][] relativeFrequencies = new int[numInstances][numClasses];

		int numEntries = (discretizedProbs.length / numInstances);

		for (int i = 0; i < discretizedProbs.length; i++) {

			// Index of the instance
			int instanceIdx = (int) (i / numEntries);

			for (int c = 0; c < numClasses - 1; c++) {
				int bin = discretizedProbs[i][c];
				histograms[instanceIdx][c][bin]++;
			}

			// Select predicted class
			int predClass = MathUtil.argmax(discretizedProbs[i]);
			relativeFrequencies[instanceIdx][predClass]++;
		}

		// Normalize the relative frequencies
		for (int i = 0; i < relativeFrequencies.length; i++) {
			for (int j = 0; j < relativeFrequencies[i].length; j++) {
				relativeFrequencies[i][j] /= numEntries;
			}
		}

		return new Pair<int[][][], int[][]>(histograms, relativeFrequencies);
	}

	/**
	 * Function discretizing probabilities into bins. The bins are determined by
	 * steps of 1 / <code>numBins</code>. The result is a matrix with the same
	 * dimensionality as <code>probs</code> storing the identifier of the
	 * corresponding bins.
	 * 
	 * @param numBins
	 *            Number of bins, determines the probability steps for each bin
	 * @param probs
	 *            Matrix storing the probabilities of each row for each class
	 *            (columns)
	 * @return Returns a matrix sharing the dimensionality of <code>probs</code>
	 *         with the discrete bin identifier
	 */
	public static int[][] discretizeProbs(final int numBins, final double[][] probs) {
		int[][] results = new int[probs.length][probs[0].length];

		final double steps = 1d / (double) numBins;

		for (int i = 0; i < results.length; i++) {
			int[] discretizedProbs = new int[probs[i].length];
			for (int j = 0; j < discretizedProbs.length; j++) {
				if (probs[i][j] == 1)
					discretizedProbs[j] = numBins - 1;
				else
					discretizedProbs[j] = (int) ((probs[i][j]) / steps);
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

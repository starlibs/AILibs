package jaicore.ml.tsc.classifier.trees;

import static jaicore.ml.tsc.features.TimeSeriesFeature.NUM_FEATURE_TYPES;
import static jaicore.ml.tsc.features.TimeSeriesFeature.getFeatures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.graph.TreeNode;
import jaicore.ml.tsc.classifier.ASimplifiedTSCAlgorithm;
import jaicore.ml.tsc.classifier.trees.TimeSeriesTree.TimeSeriesTreeNodeDecisionFunction;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.features.TimeSeriesFeature.FeatureType;
import jaicore.ml.tsc.util.TimeSeriesUtil;

/**
 * Algorithm to build a time series tree as described in Deng, Houtao et al. “A
 * Time Series Forest for Classification and Feature Extraction.” Inf. Sci. 239
 * (2013): 142-153.
 * 
 * @author Julian Lienen
 *
 */
public class TimeSeriesTreeAlgorithm extends ASimplifiedTSCAlgorithm<Integer, TimeSeriesTree> {

	/**
	 * Number of threshold candidates created in each tree recursion step.
	 */
	public static final int NUM_THRESH_CANDIDATES = 20;

	/**
	 * Alpha parameter used to weight the importance of the feature's margins to the
	 * threshold candidates.
	 */
	public static final double ENTROPY_APLHA = 0.0000000000000000000001;

	/**
	 * Precision delta used to overcome imprecision, e. g. for values very close to
	 * but not exactly zero.
	 */
	private static final double PRECISION_DELTA = 0.000000001d;

	/**
	 * Seed used for all randomized operations.
	 */
	private int seed;

	/**
	 * Maximum depth of the tree.
	 */
	private final int maxDepth;

	/**
	 * Sparse cache used for already generated feature values.
	 */
	private HashMap<Long, double[]> transformedFeaturesCache = null;
	/**
	 * Indicator whether feature caching should be used. Usage for datasets with
	 * many attributes is not recommended due to a high number of possible
	 * intervals.
	 */
	private boolean useFeatureCaching = false;

	/**
	 * Indicator that the bias (Bessel's) correction should be used for the
	 * calculation of the standard deviation.
	 */
	public static final boolean USE_BIAS_CORRECTION = true;

	/**
	 * Constructor.
	 * 
	 * @param maxDepth
	 *            Maximal depth of the tree to be trained
	 * @param seed
	 *            Seed used for randomized operations
	 * @param useFeatureCaching
	 *            Indicator whether feature caching should be used. Since feature
	 *            generation is very efficient, this should be only used if the time
	 *            series is very long
	 */
	public TimeSeriesTreeAlgorithm(final int maxDepth, final int seed, final boolean useFeatureCaching) {
		this.maxDepth = maxDepth;
		this.seed = seed;
		this.useFeatureCaching = useFeatureCaching;
	}

	/**
	 * Constructor.
	 * 
	 * @param maxDepth
	 *            Maximal depth of the tree to be trained
	 * @param seed
	 *            Seed used for randomized operations
	 */
	public TimeSeriesTreeAlgorithm(final int maxDepth, final int seed) {
		this.maxDepth = maxDepth;
		this.seed = seed;
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
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNumCPUs(int numberOfCPUs) {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTimeout(long timeout, TimeUnit timeUnit) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTimeout(TimeOut timeout) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TimeOut getTimeout() {
		// TODO Auto-generated method stub
		return null;
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
	 * Training procedure construction a time series tree using the given input
	 * data.
	 */
	@Override
	public TimeSeriesTree call()
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		// Training
		TimeSeriesDataset data = this.getInput();
		if (data.isEmpty())
			throw new IllegalArgumentException("The dataset used for training must not be null!");
		if (data.isMultivariate())
			throw new UnsupportedOperationException("Multivariate instances are not supported yet.");

		double[][] dataMatrix = data.getValuesOrNull(0);

		// Also check for number of instances
		int n = dataMatrix.length;
		if (n <= 0)
			throw new IllegalArgumentException("The traning data's matrix must contain at least one instance!");

		// Initial prior parentEntropy value, affects the scale of delta entropy values
		// in each recursion step
		double parentEntropy = 2d;

		// Set up feature caching
		if (useFeatureCaching) {
			int Q = dataMatrix[0].length;
			this.transformedFeaturesCache = new HashMap<>(Q * Q * n);
		}

		// Build tree
		tree(dataMatrix, data.getTargets(), parentEntropy, this.model.getRootNode(), 0);

		return this.model;
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

	/**
	 * Tree generation (cf. Algorithm 2 of original paper). Samples the intervals in
	 * each recursion step and calculates the features (using caches if
	 * {@link TimeSeriesTreeAlgorithm#useFeatureCaching} was set true). It then
	 * searches for an optimal split regarding several threshold candidates for
	 * feature splits. The splitting criterion is based on a metric called Entrance
	 * gain which is a combination of the entropy induced by the class proportions
	 * and the feature margins to the threshold (cf. chapter 4.1 in the paper). The
	 * tree's recursion is stopped at a leaf node if there is no entropy gain, the
	 * <code>maxDepth</code> has been reached or the local entropy is zero.
	 * 
	 * @param data
	 *            The untransformed data which will be used for the split in the
	 *            transformed feature representation
	 * @param targets
	 *            The targets of the instances
	 * @param parentEntropy
	 *            The parent entropy calculated in the recursion's previous step
	 * @param nodeToBeFilled
	 *            The tree node which should be filled with the splitting
	 *            information to use it for predictions
	 * @param depth
	 *            The current depth to be compared to the
	 *            {@link TimeSeriesTreeAlgorithm#maxDepth}
	 */
	public void tree(double[][] data, int[] targets, final double parentEntropy,
			final TreeNode<TimeSeriesTreeNodeDecisionFunction> nodeToBeFilled, int depth) {

		int n = targets.length;

		// Sample the intervals used for the feature generation
		Pair<List<Integer>, List<Integer>> T1T2 = sampleIntervals(data[0].length, this.seed);

		// Transform instances
		double[][][] transformedInstances = transformInstances(data, T1T2);
		List<List<Double>> thresholdCandidates = generateThresholdCandidates(T1T2, NUM_THRESH_CANDIDATES,
				transformedInstances);

		// Get unique classes
		final List<Integer> classes = new ArrayList<>(
				new HashSet<Integer>(Arrays.asList(ArrayUtils.toObject(targets))));

		// Initialize solution storing variables
		double deltaEntropyStar = 0, thresholdStar = 0d;
		int t1t2Star = -1;
		int fStar = -1;
		double[] eStarPerFeatureType = new double[NUM_FEATURE_TYPES];
		for (int i = 0; i < eStarPerFeatureType.length; i++) {
			eStarPerFeatureType[i] = Integer.MIN_VALUE;
		}
		double[] deltaEntropyStarPerFeatureType = new double[NUM_FEATURE_TYPES];
		int[] t1t2StarPerFeatureType = new int[NUM_FEATURE_TYPES];
		double[] thresholdStarPerFeatureType = new double[NUM_FEATURE_TYPES];

		// Search for the best splitting criterion in terms of the best Entrance gain
		// for each feature type due to different feature scales
		List<Integer> T1 = T1T2.getX();
		List<Integer> T2 = T1T2.getY();
		for (int i = 0; i < T1.size(); i++) {
			for (int k = 0; k < NUM_FEATURE_TYPES; k++) {
				for (final double cand : thresholdCandidates.get(k)) {
					// Calculate delta entropy and E for f_k(t1,t2) <= cand
					double localDeltaEntropy = calculateDeltaEntropy(transformedInstances[k][i], targets, cand, classes,
							parentEntropy);
					double localE = calculateEntrance(localDeltaEntropy,
							calculateMargin(transformedInstances[k][i], cand));

					// Update solution if it has the best Entrance value
					if (localE > eStarPerFeatureType[k]) {
						eStarPerFeatureType[k] = localE;
						deltaEntropyStarPerFeatureType[k] = localDeltaEntropy;
						t1t2StarPerFeatureType[k] = i;
						thresholdStarPerFeatureType[k] = cand;
					}
				}
			}
		}

		// Set best solution among all feature types
		int bestK = getBestSplitIndex(deltaEntropyStarPerFeatureType);
		deltaEntropyStar = deltaEntropyStarPerFeatureType[bestK];
		t1t2Star = t1t2StarPerFeatureType[bestK];
		thresholdStar = thresholdStarPerFeatureType[bestK];
		fStar = bestK;

		// Check for recursion stop condition (=> leaf node condition)
		if (Math.abs(deltaEntropyStar) <= PRECISION_DELTA || depth == maxDepth - 1
				|| (depth != 0 && Math.abs(deltaEntropyStar - parentEntropy) <= PRECISION_DELTA)) {
			// Label this node as a leaf and return majority class
			nodeToBeFilled.getValue().classPrediction = TimeSeriesUtil.getMode(targets);
			return;
		}

		// Update node's decision function
		nodeToBeFilled.getValue().f = FeatureType.values()[fStar];
		nodeToBeFilled.getValue().t1 = T1.get(t1t2Star);
		nodeToBeFilled.getValue().t2 = T2.get(t1t2Star);
		nodeToBeFilled.getValue().threshold = thresholdStar;

		// Assign data instances and the corresponding targets to the child nodes
		Pair<List<Integer>, List<Integer>> childDataIndices = getChildDataIndices(transformedInstances, n, fStar,
				t1t2Star, thresholdStar);

		double[][] dataLeft = new double[childDataIndices.getX().size()][data[0].length];
		int[] targetsLeft = new int[childDataIndices.getX().size()];
		double[][] dataRight = new double[childDataIndices.getY().size()][data[0].length];
		int[] targetsRight = new int[childDataIndices.getY().size()];

		for (int i = 0; i < childDataIndices.getX().size(); i++) {
			dataLeft[i] = data[childDataIndices.getX().get(i)];
			targetsLeft[i] = targets[childDataIndices.getX().get(i)];
		}
		for (int i = 0; i < childDataIndices.getY().size(); i++) {
			dataRight[i] = data[childDataIndices.getY().get(i)];
			targetsRight[i] = targets[childDataIndices.getY().get(i)];
		}

		// Prepare the child nodes
		TreeNode<TimeSeriesTreeNodeDecisionFunction> leftNode = nodeToBeFilled
				.addChild(new TimeSeriesTreeNodeDecisionFunction());
		TreeNode<TimeSeriesTreeNodeDecisionFunction> rightNode = nodeToBeFilled
				.addChild(new TimeSeriesTreeNodeDecisionFunction());

		// Recursion
		tree(dataLeft, targetsLeft, deltaEntropyStar, leftNode, depth + 1);
		tree(dataRight, targetsRight, deltaEntropyStar, rightNode, depth + 1);
	}

	/**
	 * Function returning the data indices assigned to the left and the right child
	 * of a binary tree based on the splitting criterion given by the feature type
	 * <code>fType</code>, the intervals index <code>t1t2</code> in the transformed
	 * data set <code>transformedData</code> and the <code>threshold</code>.
	 * 
	 * @param transformedData
	 *            Transformed data on which the split is calculated
	 * @param n
	 *            The number of instances
	 * @param fType
	 *            The feature type to be used for the split
	 * @param t1t2
	 *            The interval's index in the <code>transformedData</code> to be
	 *            used for the split
	 * @param threshold
	 *            The threshold to be used for the split
	 * @return Returns a pair of two lists, storing the data indices for the data
	 *         points assigned to the left child of the current node (X) and the
	 *         data indices assigned to the right child (Y)
	 */
	public static Pair<List<Integer>, List<Integer>> getChildDataIndices(final double[][][] transformedData,
			final int n, final int fType, final int t1t2, final double threshold) {

		List<Integer> leftIndices = new ArrayList<>();
		List<Integer> rightIndices = new ArrayList<>();

		// Check for every instance whether it should be assigned to the left or right
		// child
		for (int i = 0; i < n; i++) {
			if (transformedData[fType][t1t2][i] <= threshold)
				leftIndices.add(i);
			else
				rightIndices.add(i);
		}

		return new Pair<>(leftIndices, rightIndices);
	}

	/**
	 * Function returning feature type used for the split based on given the
	 * deltaEntropy star values. If multiple feature types have generated the same
	 * deltaEntropy value, a random decision is taken.
	 * 
	 * @param deltaEntropyStarPerFeatureType
	 *            The delta entropy star value per feature
	 * @return Returns the feature type index which has been chosen
	 */
	public int getBestSplitIndex(final double[] deltaEntropyStarPerFeatureType) {
		if (deltaEntropyStarPerFeatureType.length != NUM_FEATURE_TYPES)
			throw new IllegalArgumentException("A delta entropy star value has to be given for each feature type!");

		double max = (double) Integer.MIN_VALUE;
		List<Integer> maxIndexes = new ArrayList<>();

		// Search for the indices storing the best value
		for (int i = 0; i < deltaEntropyStarPerFeatureType.length; i++) {
			if (deltaEntropyStarPerFeatureType[i] > max) {
				max = deltaEntropyStarPerFeatureType[i];
				maxIndexes.clear();
				maxIndexes.add(i);
			} else if (deltaEntropyStarPerFeatureType[i] == max) {
				// Multiple best candidates
				maxIndexes.add(i);
			}
		}
		if (maxIndexes.size() < 1)
			throw new IllegalArgumentException(
					"Could not find any maximum delta entropy star for any feature type for the given array "
							+ Arrays.toString(deltaEntropyStarPerFeatureType) + ".");

		// Return random index among best ones if multiple solutions exist
		if (maxIndexes.size() > 1)
			Collections.shuffle(maxIndexes, new Random(this.seed));

		return maxIndexes.get(0);

	}

	/**
	 * Function calculating the delta entropy for a given
	 * <code>thresholdCandidate</code> and <code>parentEntropy</code>. The values of
	 * the data are the feature type's values for each instance. The delta entropy
	 * is formed of the difference between the parent entropy and the weighted sum
	 * of the entropy values of the children and their instance assignments based on
	 * the split.
	 * 
	 * @param dataValues
	 *            The transformed feature type values for each instance
	 * @param targets
	 *            The targets of each instance
	 * @param thresholdCandidate
	 *            The threshold candidate to be evaluated
	 * @param classes
	 *            List storing the classes whose indices can be looked up
	 * @param parentEntropy
	 *            The parent entropy used for the delta calculation
	 * @return Returns the delta entropy for the threshold candidate of the current
	 *         feature type
	 */
	public static double calculateDeltaEntropy(final double[] dataValues, final int[] targets,
			final double thresholdCandidate, final List<Integer> classes, final double parentEntropy) {

		if (dataValues.length != targets.length)
			throw new IllegalArgumentException(
					"The number of data values must be the same as the number of target values!");

		// Initialization
		double[] entropyValues = new double[2];
		int numClasses = classes.size();
		int[][] classNodeStatistic = new int[2][numClasses];
		int[] intCounter = new int[2];

		// Calculate class statistics based on the split
		for (int i = 0; i < dataValues.length; i++) {
			if (dataValues[i] <= thresholdCandidate) {
				classNodeStatistic[0][classes.indexOf(targets[i])]++;
				intCounter[0]++;
			} else {
				classNodeStatistic[1][classes.indexOf(targets[i])]++;
				intCounter[1]++;
			}
		}

		// Calculate the entropy values for each child
		for (int i = 0; i < entropyValues.length; i++) {
			double entropySum = 0;
			for (int c = 0; c < numClasses; c++) {
				double gammaC = 0;
				if (intCounter[i] != 0)
					gammaC = (double) classNodeStatistic[i][c] / (double) intCounter[i];

				entropySum += gammaC < PRECISION_DELTA ? 0 : gammaC * Math.log(gammaC);
			}
			entropyValues[i] = (-1) * entropySum;
		}

		// Get the weighted sum of the children based on the proportions of the
		// instances assigned to the corresponding nodes
		double weightedSum = 0;
		for (int i = 0; i < entropyValues.length; i++) {
			weightedSum += (double) intCounter[i] / (double) dataValues.length * entropyValues[i];
		}

		return parentEntropy - weightedSum;
	}

	/**
	 * Calculates the entrance gain specified by Deng et. al. in the paper's chapter
	 * 4.1.
	 * 
	 * @param deltaEntropy
	 *            The delta entropy
	 * @param margin
	 *            The features margin
	 * @return Returns the entrance gain
	 */
	public static double calculateEntrance(final double deltaEntropy, final double margin) {
		return deltaEntropy + ENTROPY_APLHA * margin;
	}

	/**
	 * Function calculating the margin between the given
	 * <code>thresholdCandidate</code> and the nearest feature value from the given
	 * <code>dataValues</code>.
	 * 
	 * @param dataValues
	 *            The feature values compared to the candidate
	 * @param thresholdCandidate
	 *            The threshold candidate which is assessed
	 * @return Returns the minimum distance among the feature values and the
	 *         threshold candidate
	 */
	public static double calculateMargin(final double[] dataValues, final double thresholdCandidate) {
		double min = Double.MAX_VALUE;
		for (int i = 0; i < dataValues.length; i++) {
			double localDist = Math.abs(dataValues[i] - thresholdCandidate);
			if (localDist < min)
				min = localDist;
		}
		return min;
	}

	/**
	 * Method transforming the given <code>dataset</code> using the interval pairs
	 * specified in <code>T1T2</code> by calculating each {@link FeatureType} for
	 * every instance and interval pair.
	 * 
	 * @param dataset
	 *            The dataset which should be transformed
	 * @param T1T2
	 *            The start and end interval pairs (see
	 *            {@link TimeSeriesTreeAlgorithm#sampleIntervals(int, int)})
	 * @return Returns the transformed instances (shape: number of feature types x
	 *         number of interval pairs x number of instances)
	 */
	public double[][][] transformInstances(final double[][] dataset, Pair<List<Integer>, List<Integer>> T1T2) {
		double[][][] result = new double[NUM_FEATURE_TYPES][T1T2.getX().size()][dataset.length];

		int n = dataset.length;

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < T1T2.getX().size(); j++) {

				int t1 = T1T2.getX().get(j);
				int t2 = T1T2.getY().get(j);
				double[] features;

				// If caching is used, calculate and store the generated features
				if (this.useFeatureCaching) {
					long key = i + dataset[i].length * t1 + dataset[i].length * dataset[i].length * t2;
					if (!this.transformedFeaturesCache.containsKey(key)) {
						features = getFeatures(dataset[i], t1, t2, USE_BIAS_CORRECTION);
						this.transformedFeaturesCache.put(key, features);
					} else {
						features = this.transformedFeaturesCache.get(key);
					}
				} else {
					features = getFeatures(dataset[i], t1, t2, USE_BIAS_CORRECTION);
				}

				result[0][j][i] = features[0];
				result[1][j][i] = features[1];
				result[2][j][i] = features[2];
			}
		}
		return result;
	}

	/**
	 * Function generating threshold candidates for each feature type. It calculates
	 * the interval [min f_k(t1,t2), max f_k(t1,t2)] among all instances for every
	 * feature type and every possible interval and generates
	 * <code>numberOfCandidates</code> candidates using equal-width intervals.
	 * 
	 * @param T1T2
	 *            The pair of start and end interval pairs (see
	 *            {@link TimeSeriesTreeAlgorithm#sampleIntervals(int, int)})
	 * @param numOfCandidates
	 *            The number of candidates to be generated per feature type
	 * @param transformedFeatures
	 *            The transformed data instances
	 * @return Returns a list consisting of a list for each feature type storing the
	 *         threshold candidates
	 */
	public static List<List<Double>> generateThresholdCandidates(final Pair<List<Integer>, List<Integer>> T1T2,
			final int numOfCandidates, final double[][][] transformedFeatures) {
		if (numOfCandidates < 1)
			throw new IllegalArgumentException("At least one candidate must be calculated!");

		List<List<Double>> result = new ArrayList<>();
		int numInstances = transformedFeatures[0][0].length;

		double[] min = new double[NUM_FEATURE_TYPES];
		double[] max = new double[NUM_FEATURE_TYPES];

		// Initialize
		for (int i = 0; i < NUM_FEATURE_TYPES; i++) {
			result.add(new ArrayList<>());
			min[i] = Double.MAX_VALUE;
			max[i] = Integer.MIN_VALUE;
		}

		// Find min and max
		for (int i = 0; i < NUM_FEATURE_TYPES; i++) {
			for (int j = 0; j < numInstances; j++) {
				for (int l = 0; l < T1T2.getX().size(); l++) {
					if (transformedFeatures[i][l][j] < min[i])
						min[i] = transformedFeatures[i][l][j];
					if (transformedFeatures[i][l][j] > max[i])
						max[i] = transformedFeatures[i][l][j];
				}
			}
		}

		// Calculate equal-width candidate threshold
		for (int i = 0; i < NUM_FEATURE_TYPES; i++) {
			double width = (max[i] - min[i]) / (numOfCandidates + 1);
			for (int j = 0; j < numOfCandidates; j++) {
				result.get(i).add(min[i] + (j + 1) * width);
			}
		}

		return result;
	}

	/**
	 * Function sampling intervals based on the length of the time series
	 * <code>m</code> and the given <code>seed</code>. Refers to algorithm 1 of the
	 * paper. The sampled intervals are stored in a pair of lists where each index
	 * of the first list is related to the same index in the second list. Sampling
	 * is done without replacement.
	 * 
	 * @param m
	 *            Number of time series attributes (steps)
	 * @param seed
	 *            The seed used for the randomized sampling
	 * @return Returns a pair of lists consisting of the start indices (X) and the
	 *         end indices (Y)
	 */
	public static Pair<List<Integer>, List<Integer>> sampleIntervals(final int m, final int seed) {
		if (m < 1)
			throw new IllegalArgumentException("The series' length m must be greater than zero.");

		List<Integer> T1 = new ArrayList<>();
		List<Integer> T2 = new ArrayList<>();
		List<Integer> W = randomlySampleNoReplacement(IntStream.rangeClosed(1, m).boxed().collect(Collectors.toList()),
				(int) Math.sqrt(m), seed);
		for (int w : W) {
			List<Integer> tmpSampling = randomlySampleNoReplacement(
					IntStream.rangeClosed(0, m - w).boxed().collect(Collectors.toList()), (int) Math.sqrt(m - w + 1),
					seed);
			T1.addAll(tmpSampling);
			for (int t1 : tmpSampling) {
				T2.add(t1 + w - 1);
			}
		}
		return new Pair<List<Integer>, List<Integer>>(T1, T2);
	}

	/**
	 * Function sampling a given <code>list</code> randomly without replacement
	 * using the given <code>seed</code>. <code>sampleSize</code> many elements are
	 * sampled and returned.
	 * 
	 * @param list
	 *            List to be sampled from without replacement
	 * @param sampleSize
	 *            Number of elements to be sampled (must be <= list.size())
	 * @param seed
	 *            The seed used for the randomized sampling
	 * @return Returns a list of elements which have been sampled
	 */
	public static List<Integer> randomlySampleNoReplacement(final List<Integer> list, final int sampleSize,
			final int seed) {
		if (list == null)
			throw new IllegalArgumentException("The list to be sampled from must not be null!");
		if (sampleSize < 1 || sampleSize > list.size())
			throw new IllegalArgumentException(
					"Sample size must lower equals the size of the list to be sampled from without replacement and greater zero.");

		final List<Integer> listCopy = new ArrayList<>(list);
		Collections.shuffle(listCopy, new Random(seed));

		return listCopy.subList(0, sampleSize);
	}


}

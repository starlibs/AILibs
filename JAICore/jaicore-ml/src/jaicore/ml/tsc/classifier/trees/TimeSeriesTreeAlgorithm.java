package jaicore.ml.tsc.classifier.trees;

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
import jaicore.ml.tsc.util.TimeSeriesUtil;

public class TimeSeriesTreeAlgorithm extends ASimplifiedTSCAlgorithm<Integer, TimeSeriesTree> {

	public enum FeatureType {
		MEAN, STDDEV, SLOPE
	}

	public static final int NUM_FEATURE_TYPES = 3;
	public static final int NUM_THRESH_CANDIDATES = 20;

	// Set to useful value
	public static final double ENTROPY_APLHA = 0.0000000000000000000001;

	private static final double PRECISION_DELTA = 0.000000001d;

	private int seed;

	private final int maxDepth;

	// Caching mechanism
	private HashMap<Long, double[]> transformedFeaturesCache = null;
	private boolean useFeatureCaching = false;

	public TimeSeriesTreeAlgorithm(final int maxDepth, final int seed, final boolean useFeatureCaching) {
		this.maxDepth = maxDepth;
		this.seed = seed;
		this.useFeatureCaching = useFeatureCaching;
	}

	public TimeSeriesTreeAlgorithm(final int maxDepth, final int seed) {
		this.maxDepth = maxDepth;
		this.seed = seed;
	}

	@Override
	public void registerListener(Object listener) {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	@Override
	public int getNumCPUs() {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	@Override
	public void setTimeout(long timeout, TimeUnit timeUnit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTimeout(TimeOut timeout) {
		// TODO Auto-generated method stub

	}

	@Override
	public TimeOut getTimeout() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AlgorithmEvent nextWithException()
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	@Override
	public IAlgorithmConfig getConfig() {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	@Override
	public TimeSeriesTree call()
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {

		// Training

		TimeSeriesDataset data = this.getInput();
		double[][] dataMatrix = data.getValuesOrNull(0);

		int n = dataMatrix.length;
		if (n <= 0)
			throw new IllegalArgumentException("The traning data must contain at least one instance!");

		// TODO: Does this make sense?
		double parentEntropy = .5d;

		if (useFeatureCaching) {
			int Q = dataMatrix[0].length;
			this.transformedFeaturesCache = new HashMap<>(Q * Q * n);
		}

		tree(dataMatrix, data.getTargets(), parentEntropy, this.model.getRootNode(), 0);

		return null;
	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AlgorithmEvent next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	// Entropy based
	public void tree(double[][] data, int[] targets, final double parentEntropy,
			final TreeNode<TimeSeriesTreeNodeDecisionFunction> nodeToBeFilled, int depth) {

		Pair<List<Integer>, List<Integer>> T1T2 = sampleIntervals(data[0].length, this.seed);

		// Transform instances
		double[][][] transformedInstances = transformInstances(data, T1T2);
		List<List<Double>> thresholdCandidates = generateThresholdCandidates(T1T2, NUM_THRESH_CANDIDATES,
				transformedInstances);

		int n = targets.length;

		// Get unique classes
		final List<Integer> classes = new ArrayList<>(
				new HashSet<Integer>(Arrays.asList(ArrayUtils.toObject(targets))));

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

					if (localE > eStarPerFeatureType[k]) {
						eStarPerFeatureType[k] = localE;
						deltaEntropyStarPerFeatureType[k] = localDeltaEntropy;
						t1t2StarPerFeatureType[k] = i;
						thresholdStarPerFeatureType[k] = cand;
					}
				}
			}
			// }
		}

		// Set best solution
		int bestK = getBestSplitIndex(deltaEntropyStarPerFeatureType);
		// eStar = eStarPerFeatureType[bestK];
		deltaEntropyStar = deltaEntropyStarPerFeatureType[bestK];
		t1t2Star = t1t2StarPerFeatureType[bestK];
		thresholdStar = thresholdStarPerFeatureType[bestK];
		fStar = bestK;

		//
		if (Math.abs(deltaEntropyStar) <= PRECISION_DELTA || depth == maxDepth - 1
				|| (depth != 0 && Math.abs(deltaEntropyStar - parentEntropy) <= PRECISION_DELTA)) {
			// Label this node as a leaf and return
			// Get majority
			nodeToBeFilled.getValue().classPrediction = TimeSeriesUtil.getMode(targets);
			return;
		}

		// Update node's decision function
		nodeToBeFilled.getValue().f = FeatureType.values()[fStar];
		nodeToBeFilled.getValue().t1 = T1.get(t1t2Star);
		nodeToBeFilled.getValue().t2 = T2.get(t1t2Star);
		nodeToBeFilled.getValue().threshold = thresholdStar;

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

		TreeNode<TimeSeriesTreeNodeDecisionFunction> leftNode = nodeToBeFilled
				.addChild(new TimeSeriesTreeNodeDecisionFunction());
		TreeNode<TimeSeriesTreeNodeDecisionFunction> rightNode = nodeToBeFilled
				.addChild(new TimeSeriesTreeNodeDecisionFunction());

		tree(dataLeft, targetsLeft, deltaEntropyStar, leftNode, depth + 1);
		tree(dataRight, targetsRight, deltaEntropyStar, rightNode, depth + 1);
	}

	public static Pair<List<Integer>, List<Integer>> getChildDataIndices(final double[][][] transformedData,
			final int n, final int k, final int t1t2, final double threshold) {

		List<Integer> leftIndices = new ArrayList<>();
		List<Integer> rightIndices = new ArrayList<>();

		for (int i = 0; i < n; i++) {
			if (transformedData[k][t1t2][i] <= threshold)
				leftIndices.add(i);
			else
				rightIndices.add(i);
		}

		return new Pair<>(leftIndices, rightIndices);
	}

	public int getBestSplitIndex(final double[] deltaEntropyStarPerFeatureType) {
		double max = (double) Integer.MIN_VALUE;

		List<Integer> maxIndexes = new ArrayList<>();

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

	// Assume targets 1 to n
	public static double calculateDeltaEntropy(final double[] dataValues, final int[] targets,
			final double thresholdCandidate, final List<Integer> classes, final double parentEntropy) {
		double[] entropyValues = new double[2];

		int numClasses = classes.size();

		int[][] classNodeStatistic = new int[2][numClasses];
		int[] intCounter = new int[2];

		// Calculate proportions
		for (int i = 0; i < dataValues.length; i++) {
			if (dataValues[i] <= thresholdCandidate) {
				classNodeStatistic[0][classes.indexOf(targets[i])]++;
				intCounter[0]++;
			} else {
				classNodeStatistic[1][classes.indexOf(targets[i])]++;
				intCounter[1]++;
			}
		}

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

		double weightedSum = 0;
		for (int i = 0; i < entropyValues.length; i++) {
			weightedSum += (double) intCounter[i] / (double) dataValues.length * entropyValues[i];
		}

		return parentEntropy - weightedSum;
	}

	public static double calculateEntrance(final double deltaEntropy, final double margin) {
		return deltaEntropy + ENTROPY_APLHA * margin;
	}

	public static double calculateMargin(final double[] dataValues, final double thresholdCandidate) {
		double min = Double.MAX_VALUE;
		for (int i = 0; i < dataValues.length; i++) {
			double localDist = Math.abs(dataValues[i] - thresholdCandidate);
			if (localDist < min)
				min = localDist;
		}

		return min;
	}

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
						features = getFeatures(dataset[i], t1, t2);
						this.transformedFeaturesCache.put(key, features);
					} else {
						features = this.transformedFeaturesCache.get(key);
					}
				} else {
					features = getFeatures(dataset[i], t1, t2);
				}

				result[0][j][i] = features[0];
				result[1][j][i] = features[1];
				result[2][j][i] = features[2];
			}
		}
		return result;
	}

	// TODO: Make enum out of feature type
	public static List<List<Double>> generateThresholdCandidates(final Pair<List<Integer>, List<Integer>> T1T2,
			final int numOfCandidates, final double[][][] transformedFeatures) {
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

	// t2 inclusive
	public static double[] getFeatures(final double[] vector, final int t1, final int t2) {
		double[] result = new double[NUM_FEATURE_TYPES];

		if (t1 >= vector.length || t2 >= vector.length)
			throw new IllegalArgumentException("Parameters t1 and t2 must be valid indices of the vector.");

		if (t1 == t2)
			return new double[] { vector[t1], 0d, 0d };

		// Calculate running sums for mean, stddev and slope
		double xx = 0;
		double x = 0;
		double xy = 0;
		double y = 0;
		double yy = 0;

		for (int i = t1; i <= t2; i++) {
			x += i;
			y += vector[i];
			yy += vector[i] * vector[i];
			xx += i * i;
			xy += i * vector[i];
		}
		result[0] = y / (double) (t2 - t1 + 1);
		
		// TODO: Use Bessel's correction?
		double length = t2 - t1 + 1d;
		double stddev = (yy / length - ((y / length) * (y / length)));
		stddev *= length / (length - 1);
		stddev = Math.sqrt(stddev);
		result[1] = stddev;

		// Calculate slope
		result[2] = (length * xy - x * y) / (length * xx - x * x);
		return result;
	}



	public static double calculateFeature(final FeatureType fType, final double[] instance, final int t1,
			final int t2) {
		switch (fType) {
		case MEAN:
			return TimeSeriesUtil.mean(instance, t1, t2);
		case STDDEV:
			return TimeSeriesUtil.stddev(instance, t1, t2);
		case SLOPE:
			return TimeSeriesUtil.slope(instance, t1, t2);
		default:
			throw new UnsupportedOperationException(
					"Feature calculation function with id '" + fType + "' is unknwon.");
		}
	}

}

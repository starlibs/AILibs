package jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.sets.SetUtil.Pair;

// As proposed by Deng et. al. (as opposed to simplified version by Bagnall et. al.)
public class TimeSeriesForestAlgorithm extends ASimplifiedTSCAlgorithm<Integer, TimeSeriesForestClassifier> {

	public static final int NUM_FEATURE_TYPES = 3;
	public static final int NUM_THRESH_CANDIDATES = 20;

	// Set to useful value
	private static final double ENTROPY_APLHA = 0.01;

	private static final double PRECISION_DELTA = 0.000001d;

	private int seed;

	@Override
	public void registerListener(Object listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumCPUs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAlgorithmConfig getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TimeSeriesForestClassifier call()
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		// TODO Auto-generated method stub
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
	public void tree(double[][] data, int[] targets, final double parentEntropy) {
		Pair<List<Integer>, List<Integer>> T1T2 = sampleIntervals(data[0].length);
		
		double[][][][] transformedInstances = transformInstances(data, T1T2);
		List<List<Double>> thresholdCandidates = generateThresholdCandidates(T1T2, NUM_THRESH_CANDIDATES,
				transformedInstances);

		// TODO
		int C = 0;
		
		// Transform instances

		// TODO: Calculate the thresholds for each feature type k
		// double[] thresholds = new double[NUM_FEATURE_TYPES];
		// for (int i = 0; i < NUM_FEATURE_TYPES; i++) {
		// thresholds[i] = 0;
		// }

		double eStar = 0, deltaEntropyStar = 0, thresholdStar = 0d;
		int t1Star = 0, t2Star = 0;
		int fStar = -1;
		
		double[] eStarPerFeatureType = new double[NUM_FEATURE_TYPES];
		double[] deltaEntropyStarPerFeatureType = new double[NUM_FEATURE_TYPES];
		int[] t1StarPerFeatureType = new int[NUM_FEATURE_TYPES];
		int[] t2StarPerFeatureType = new int[NUM_FEATURE_TYPES];
		double[] thresholdStarPerFeatureType = new double[NUM_FEATURE_TYPES];

		// TODO: Scale all the features 
		for (final int t1 : T1T2.getX()) {
			for (final int t2 : T1T2.getY()) {
				for (int k = 0; k < NUM_FEATURE_TYPES; k++) {
					for (final double cand : thresholdCandidates.get(k)) {
						// TOOD: Calculate delta entropy and E for f_k(t1,t2) <= cand
						double localDeltaEntropy = calculateDeltaEntropy(transformedInstances[k][t1][t2], targets, cand,
								C,
								parentEntropy);
						double localE = calculateEntrance(localDeltaEntropy,
								calculateMargin(transformedInstances[k][t1][t2], cand), ENTROPY_APLHA);
						
						if (localE > eStarPerFeatureType[k]) {
							eStarPerFeatureType[k] = localE;
							deltaEntropyStarPerFeatureType[k] = localDeltaEntropy;
							t1StarPerFeatureType[k] = t1;
							t2StarPerFeatureType[k] = t2;
							thresholdStarPerFeatureType[k] = cand;
						}
					}
				}
			}
		}
		
		// Set best solution
		int bestK = getBestSplitIndex(deltaEntropyStarPerFeatureType);
		eStar = eStarPerFeatureType[bestK];
		deltaEntropyStar = deltaEntropyStarPerFeatureType[bestK];
		t1Star = t1StarPerFeatureType[bestK];
		t2Star = t2StarPerFeatureType[bestK];
		thresholdStar = thresholdStarPerFeatureType[bestK];
		fStar = bestK;

		if (Math.abs(deltaEntropyStar) <= PRECISION_DELTA) {
			// Label this node as a leaf and return
			// TODO
		}
		
		double[][] dataLeft = null; // TODO
		int[] targetsLeft = null; // TODO
		double[][] dataRight = null; // TODO
		int[] targetsRight = null; // TODO

		tree(dataLeft, targetsLeft, deltaEntropyStar);
		tree(dataRight, targetsRight, deltaEntropyStar);
	}

	public int getBestSplitIndex(final double[] deltaEntropyStarPerFeatureType) {
		double max = Double.MIN_VALUE;

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
		if(maxIndexes.size() < 1)
			throw new IllegalArgumentException("Could not find any maximum delta entropy star for any feature type for the given array " + Arrays.toString(deltaEntropyStarPerFeatureType) + ".");
		
		// Return random index among best ones if multiple solutions exist
		if (maxIndexes.size() > 1)
			Collections.shuffle(maxIndexes);

		return maxIndexes.get(0);

	}

	// Assume targets 1 to n
	public static double calculateDeltaEntropy(final double[] dataValues, final int[] targets,
			final double thresholdCandidate, final int numClasses, final double parentEntropy) {
		// TODO
		double[] entropyValues = new double[2];

		int[][] classNodeStatistic = new int[2][numClasses];
		int[] intCounter = new int[2];

		// Calculate proportions
		for (int i = 0; i < dataValues.length; i++) {
			if (dataValues[i] < thresholdCandidate) {
				classNodeStatistic[0][targets[i]]++;
				intCounter[0]++;
			} else {
				classNodeStatistic[1][targets[i]]++;
				intCounter[1]++;
			}
		}

		for (int i = 0; i < entropyValues.length; i++) {
			double entropySum = 0;
			for(int c=0; c<numClasses; c++) {
				entropySum += (double) classNodeStatistic[i][c] / (double) intCounter[i];
			}
			entropyValues[i] = (-1) * entropySum;
		}
		
		double weightedSum = 0;
		for (int i = 0; i < entropyValues.length; i++) {
			weightedSum += (double) intCounter[i] / (double) dataValues.length * entropyValues[i];
		}
		
		return parentEntropy - weightedSum;
	}

	public static double calculateEntrance(final double deltaEntropy, final double margin, final double alpha) {
		return deltaEntropy + alpha * margin;
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

	public static double[][][][] transformInstances(final double[][] dataset, Pair<List<Integer>, List<Integer>> T1T2) {
		double[][][][] result = new double[dataset.length][NUM_FEATURE_TYPES][T1T2.getX().size()][T1T2.getY().size()];

		for (int i = 0; i < dataset.length; i++) {
			for (int k = 0; k < T1T2.getX().size(); k++) {
				for (int l = 0; l < T1T2.getY().size(); l++) {
					double[] features = getFeatures(dataset[i], T1T2.getX().get(k), T1T2.getX().get(l));
					result[0][k][l][i] = features[0];
					result[1][k][l][i] = features[1];
					result[2][k][l][i] = features[2];
				}
			}
		}
		return result;
	}

	// TODO: Make enum out of feature type
	public List<List<Double>> generateThresholdCandidates(final Pair<List<Integer>, List<Integer>> T1T2,
			final int numOfCandidates, final double[][][][] transformedFeatures) {
		List<List<Double>> result = new ArrayList<>();

		int numInstances = transformedFeatures[0][0][0].length;

		double[] min = new double[NUM_FEATURE_TYPES];
		double[] max = new double[NUM_FEATURE_TYPES];

		// Initialize
		for (int i = 0; i < NUM_FEATURE_TYPES; i++) {
			result.add(new ArrayList<>());
			min[i] = Double.MAX_VALUE;
			max[i] = Double.MIN_VALUE;
		}

		// Find min and max
		for(int i=0; i<NUM_FEATURE_TYPES; i++) {
			for (int j = 0; j < numInstances; j++) {
				for (final int t1 : T1T2.getX()) {
					for (final int t2 : T1T2.getY()) {
						if (transformedFeatures[i][t1][t2][j] < min[i])
							min[i] = transformedFeatures[i][t1][t2][j];
						if (transformedFeatures[i][t1][t2][j] > max[i])
							max[i] = transformedFeatures[i][t1][t2][j];
					}
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

	public Pair<List<Integer>, List<Integer>> sampleIntervals(final int m) {
		if(m<1)
			throw new IllegalArgumentException("The series' length m must be greater than zero.");
		
		List<Integer> T1 = new ArrayList<>();
		List<Integer> T2 = new ArrayList<>();
		List<Integer> W = randomlySampleNoReplacement(IntStream.range(0, m).boxed().collect(Collectors.toList()),
				(int) Math.sqrt(m), this.seed);
		for (int w : W) {
			List<Integer> tmpSampling = randomlySampleNoReplacement(
					IntStream.range(0, m - w + 1).boxed().collect(Collectors.toList()), (int) Math.sqrt(m - w + 1),
					this.seed);
			T1.addAll(tmpSampling);
			for(int t1 : tmpSampling) {
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

	public static double[] getFeatures(final double[] vector, final int t1, final int t2) {
		double[] result = new double[NUM_FEATURE_TYPES];

		// Calculate mean
		// TODO: Iteratively calculating mean AND stddev
		result[0] = getMean(vector, t1, t2);
		
		double xx = 0;
		double x = 0;
		double xy = 0;
		double y =0;

		double stddev = 0;
		for (int i = t1; i < t2; i++) {
			stddev += Math.pow(vector[i] - result[0], 2);
			
			x += i;
			y += vector[i];
			xx += i * i;
			xy += i * vector[i];
		}
		result[1] = Math.sqrt(stddev / (t2 - t1));
		
		// Calculate slope
		int length = t2-t1;
		result[2] = (length * xy - x * y) / (length * xx - x * x);
		return result;
	}

	private static double getMean(final double[] vector, final int t1, final int t2) {
		double result = 0;
		for (int i = t1; i < t2; i++) {
			result += vector[i];
		}
		return result / (t2 - t1 + 1);
	}

	private static double getStddev(final double[] vector, final int t1, final int t2) {
		if (t1 == t2)
			return 0.0d;

		double mean = getMean(vector, t1, t2);

		double result = 0;
		for (int i = t1; i < t2; i++) {
			result += Math.pow(vector[i] - mean, 2);
		}

		return Math.sqrt(result / (t2 - t1));
	}
	//
	// private static double getSlope(final double[] vector, final int t1, final int
	// t2) {
	// if (t1 == t2)
	// return 0.0d;
	//
	//
	// }
}

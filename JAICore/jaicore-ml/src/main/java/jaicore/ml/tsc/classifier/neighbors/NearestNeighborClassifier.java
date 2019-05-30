package jaicore.ml.tsc.classifier.neighbors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import org.aeonbits.owner.ConfigCache;

import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.classifier.ASimplifiedTSClassifier;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.distances.ITimeSeriesDistance;

/**
 * K-Nearest-Neighbor classifier for time series.
 *
 * Given an integer <code>k</code>, a distance measure <code>d</code>
 * ({@link jaicore.ml.tsc.distances}), a training set of time series
 * <code>TRAIN = {(x, y)}</code> and a test time series <code>T</code> (or a set
 * of test time series).
 * <p>
 * The set of k nearest neighbors <code>NN</code> for <code>T</code> is a subset
 * (or equal) of <code>TRAIN</code> with cardinality <code>k</code> such that
 * for all <code>(T, S)</code> with <code>S</code> in <code>TRAIN\NN</code>
 * holds <code>d(S, T) >= max_{T' in NN} d(S, T')</code>.
 * </p>
 * From the labels of the instances in <code>NN</code> the label for
 * <code>T</code> is aggregated, e.g. via majority vote.
 *
 * @author fischor
 */
public class NearestNeighborClassifier extends ASimplifiedTSClassifier<Integer> {

	/**
	 * Votes types that describe how to aggregate the prediciton for a test instance
	 * on its nearest neighbors found.
	 */
	public enum VoteType {
		/**
		 * Majority vote with @see NearestNeighborClassifier#voteMajority.
		 */
		MAJORITY,
		/**
		 * Weighted stepwise vote with @see
		 * NearestNeighborClassifier#voteWeightedStepwise.
		 */
		WEIGHTED_STEPWISE,
		/**
		 * Weighted proportional to distance vote with @see
		 * NearestNeighborClassifier#voteWeightedProportionalToDistance.
		 */
		WEIGHTED_PROPORTIONAL_TO_DISTANCE,
	}

	/**
	 * Comparator class for the nearest neighbor priority queues, used for the
	 * nearest neighbor calculation. Sorts pairs of
	 * <code>(Integer: targetClass, Double: distance)</code> for nearest neigbors by
	 * distance ascending.
	 */
	private static class NearestNeighborComparator implements Comparator<Pair<Integer, Double>> {

		@Override
		public int compare(final Pair<Integer, Double> o1, final Pair<Integer, Double> o2) {
			return -1 * o1.getY().compareTo(o2.getY());
		}

	}

	/**
	 * Singleton comparator instance for the nearest neighbor priority queues, used
	 * for the nearest neighbor calculation.
	 */
	protected static final NearestNeighborComparator nearestNeighborComparator = new NearestNeighborComparator();

	/** Number of neighbors. */
	private int k;

	/** Distance measure. */
	private ITimeSeriesDistance distanceMeasure;

	/** Type of the voting. */
	private VoteType voteType;

	/** Value matrix containing the time series instances. Set by algorithm. */
	protected double[][] values;

	/**
	 * Timestamp matrix containing the timestamps of the instances. Set by the
	 * algorihm.
	 */
	protected double[][] timestamps;

	/** Target values for the instances. Set by the algorithm. */
	protected int[] targets;

	/**
	 * Creates a k nearest neighbor classifier.
	 *
	 * @param k               The number of nearest neighbors.
	 * @param distanceMeasure Distance measure for calculating the distances between
	 *                        every pair of train and test instances.
	 * @param voteType        Vote type to use to aggregate the the classes of the
	 *                        the k nearest neighbors into a single class
	 *                        prediction.
	 */
	public NearestNeighborClassifier(final int k, final ITimeSeriesDistance distanceMeasure, final VoteType voteType) {

		// Parameter checks.
		if (distanceMeasure == null) {
			throw new IllegalArgumentException("Distance measure must not be null");
		}
		if (voteType == null) {
			throw new IllegalArgumentException("Vote type must not be null.");
		}

		// Set attributes.
		this.distanceMeasure = distanceMeasure;
		this.k = k;
		this.voteType = voteType;
	}

	/**
	 * Creates a k nearest neighbor classifier using majority vote.
	 *
	 * @param k               The number of nearest neighbors.
	 * @param distanceMeasure Distance measure for calculating the distances between
	 *                        every pair of train and test instances.
	 */
	public NearestNeighborClassifier(final int k, final ITimeSeriesDistance distanceMeasure) {
		this(k, distanceMeasure, VoteType.MAJORITY);
	}

	/**
	 * Creates a 1 nearest neighbor classifier using majority vote.
	 *
	 * @param distanceMeasure Distance measure for calculating the distances between
	 *                        every pair of train and test instances.
	 */
	public NearestNeighborClassifier(final ITimeSeriesDistance distanceMeasure) {
		this(1, distanceMeasure, VoteType.MAJORITY);
	}

	/**
	 * Predicts on univariate instance.
	 *
	 * @param univInstance The univariate instance.
	 * @return Class prediction for the instance.
	 */
	@Override
	public Integer predict(final double[] univInstance) throws PredictionException {
		if (univInstance == null) {
			throw new IllegalArgumentException("Instance to predict must not be null.");
		}
		return this.calculatePrediction(univInstance);
	}

	/**
	 * Predicts on a multivariate instance. This is not supported yet.
	 *
	 * @param multivInstance The multivariate instance.
	 * @return Class prediciton for the instance.
	 */
	@Override
	public Integer predict(final List<double[]> multivInstance) throws PredictionException {
		throw new PredictionException("Can't predict on multivariate data yet.");
	}

	/**
	 * Predicts on a dataset.
	 *
	 * @param dataset The dataset.
	 * @return List of class predicitons for each instance of the dataset.
	 */
	@Override
	public List<Integer> predict(final TimeSeriesDataset dataset) throws PredictionException {
		// Parameter checks.
		if (dataset == null) {
			throw new IllegalArgumentException("Dataset must not be null.");
		}
		double[][] testInstances = dataset.getValuesOrNull(0);
		if (testInstances == null) {
			throw new PredictionException("Can't predict on empty dataset.");
		}
		// Calculate predictions.
		ArrayList<Integer> predictions = new ArrayList<>(dataset.getNumberOfInstances());
		for (double[] testInstance : testInstances) {
			int prediction = this.calculatePrediction(testInstance);
			predictions.add(prediction);
		}
		return predictions;
	}

	/**
	 * Calculates predicition on a single test instance.
	 *
	 * @param testInstance The test instance (not null assured within class).
	 * @return
	 */
	protected int calculatePrediction(final double[] testInstance) {
		// Determine the k nearest neighbors for the test instance.
		PriorityQueue<Pair<Integer, Double>> nearestNeighbors = this.calculateNearestNeigbors(testInstance);
		// Vote on determined neighbors to create prediction and return prediction.
		int prediction = this.vote(nearestNeighbors);
		return prediction;
	}

	/**
	 * Determine the k nearest neighbors for a test instance.
	 *
	 * @param testInstance The time series to determine the k nearest neighbors for.
	 * @return Queue of the k nearest neighbors as pairs (class, distance).
	 */
	protected PriorityQueue<Pair<Integer, Double>> calculateNearestNeigbors(final double[] testInstance) {
		int numberOfTrainInstances = this.values.length;
		// Priority queue of (class, distance)-pairs for nearest neigbors, sorted by
		// distance ascending.
		PriorityQueue<Pair<Integer, Double>> nearestNeighbors = new PriorityQueue<>(nearestNeighborComparator);

		// Calculate the k nearest neighbors.
		for (int i = 0; i < numberOfTrainInstances; i++) {
			double d = this.distanceMeasure.distance(testInstance, this.values[i]);

			Pair<Integer, Double> neighbor = new Pair<>(this.targets[i], d);
			nearestNeighbors.add(neighbor);
			if (nearestNeighbors.size() > this.k) {
				nearestNeighbors.poll();
			}
		}
		return nearestNeighbors;
	}

	/**
	 * Performs a vote on the nearest neighbors found. Delegates the vote according
	 * to the vote type.
	 *
	 * @param nearestNeighbors Priority queue of (class, distance)-pairs for nearest
	 *                         neigbors, sorted by distance ascending. (Not null
	 *                         assured within class)
	 * @return Result of the vote, i.e. the predicted class.
	 */
	protected int vote(final PriorityQueue<Pair<Integer, Double>> nearestNeighbors) {
		switch (this.voteType) {
		case WEIGHTED_STEPWISE:
			return this.voteWeightedStepwise(nearestNeighbors);
		case WEIGHTED_PROPORTIONAL_TO_DISTANCE:
			return this.voteWeightedProportionalToDistance(nearestNeighbors);
		case MAJORITY:
		default:
			return this.voteMajority(nearestNeighbors);
		}
	}

	/**
	 * Performs a vote with stepwise weights 1, 2, .., k on the set nearest
	 * neighbors found.
	 *
	 * @param nearestNeighbors Priority queue of (class, distance)-pairs for nearest
	 *                         neigbors, sorted by distance ascending. (Not null
	 *                         assured within class)
	 * @return Result of the vote, i.e. the predicted class.
	 */
	protected int voteWeightedStepwise(final PriorityQueue<Pair<Integer, Double>> nearestNeighbors) {
		// Voting.
		HashMap<Integer, Integer> votes = new HashMap<>();
		int weight = 1;
		while (!nearestNeighbors.isEmpty()) {
			Pair<Integer, Double> neighbor = nearestNeighbors.poll();
			Integer targetClass = neighbor.getX();
			Integer currentVotesOnTargetClass = votes.get(targetClass);
			if (currentVotesOnTargetClass == null) {
				votes.put(targetClass, weight);
			} else {
				votes.put(targetClass, currentVotesOnTargetClass + weight);
			}
			weight++;
		}
		// Return most voted target (class that got most weights).
		Integer maxWeightOfVotes = Integer.MIN_VALUE;
		Integer mostVotedTargetClass = -1;
		for (Integer targetClass : votes.keySet()) {
			Integer votedWeightsForTargetClass = votes.get(targetClass);
			System.out.print(targetClass + " -> " + votedWeightsForTargetClass);
			if (votedWeightsForTargetClass > maxWeightOfVotes) {
				maxWeightOfVotes = votedWeightsForTargetClass;
				mostVotedTargetClass = targetClass;
			}
		}
		return mostVotedTargetClass;
	}

	/**
	 * Performs a vote with weights proportional to the distance on the set nearest
	 * neighbors found.
	 *
	 * @param nearestNeighbors Priority queue of (class, distance)-pairs for nearest
	 *                         neigbors, sorted by distance ascending. (Not null
	 *                         assured within class)
	 * @return Result of the vote, i.e. the predicted class.
	 */
	protected int voteWeightedProportionalToDistance(final PriorityQueue<Pair<Integer, Double>> nearestNeighbors) {
		// Voting.
		HashMap<Integer, Double> votes = new HashMap<>();
		for (Pair<Integer, Double> neighbor : nearestNeighbors) {
			Integer targetClass = neighbor.getX();
			double distance = neighbor.getY();
			Double currentVotesOnTargetClass = votes.get(targetClass);
			if (currentVotesOnTargetClass == null) {
				votes.put(targetClass, 1.0 / distance);
			} else {
				votes.put(targetClass, currentVotesOnTargetClass + 1.0 / distance);
			}
		}
		// Return most voted target (class that got most weights).
		Double maxWeightOfVotes = Double.MIN_VALUE;
		Integer mostVotedTargetClass = -1;
		for (Integer targetClass : votes.keySet()) {
			Double votedWeightsForTargetClass = votes.get(targetClass);
			if (votedWeightsForTargetClass > maxWeightOfVotes) {
				maxWeightOfVotes = votedWeightsForTargetClass;
				mostVotedTargetClass = targetClass;
			}
		}
		return mostVotedTargetClass;
	}

	/**
	 * Performs a majority vote on the set nearest neighbors found.
	 *
	 * @param nearestNeighbors Priority queue of (class, distance)-pairs for nearest
	 *                         neigbors, sorted by distance ascending. (Not null
	 *                         assured within class)
	 * @return Result of the vote, i.e. the predicted class.
	 */
	protected int voteMajority(final PriorityQueue<Pair<Integer, Double>> nearestNeighbors) {
		// Voting.
		HashMap<Integer, Integer> votes = new HashMap<>();
		for (Pair<Integer, Double> neighbor : nearestNeighbors) {
			Integer targetClass = neighbor.getX();
			Integer currentVotesOnTargetClass = votes.get(targetClass);
			if (currentVotesOnTargetClass == null) {
				votes.put(targetClass, 1);
			} else {
				votes.put(targetClass, currentVotesOnTargetClass + 1);
			}
		}
		// Return most voted target.
		Integer maxNumberOfVotes = Integer.MIN_VALUE;
		Integer mostVotedTargetClass = -1;
		for (Integer targetClass : votes.keySet()) {
			Integer numberOfVotesForTargetClass = votes.get(targetClass);
			if (numberOfVotesForTargetClass > maxNumberOfVotes) {
				maxNumberOfVotes = numberOfVotesForTargetClass;
				mostVotedTargetClass = targetClass;
			}
		}
		return mostVotedTargetClass;
	}

	/**
	 * Sets the value matrix.
	 *
	 * @param values
	 */
	protected void setValues(final double[][] values) {
		if (values == null) {
			throw new IllegalArgumentException("Values must not be null");
		}
		this.values = values;
	}

	/**
	 * Sets the timestamps.
	 *
	 * @param timestamps
	 */
	protected void setTimestamps(final double[][] timestamps) {
		this.timestamps = timestamps;
	}

	/**
	 * Sets the targets.
	 *
	 * @param targets
	 */
	protected void setTargets(final int[] targets) {
		if (targets == null) {
			throw new IllegalArgumentException("Targets must not be null");
		}
		this.targets = targets;
	}

	/**
	 * Getter for the k value, @see #k.
	 *
	 * @return k
	 */
	public int getK() {
		return this.k;
	}

	/**
	 * Getter for the vote type. @see #voteType.
	 *
	 * @return The vote type.
	 */
	public VoteType getVoteType() {
		return this.voteType;
	}

	/**
	 * Getter for the distance measure. @see #distanceMeasure.
	 *
	 * @return
	 */
	public ITimeSeriesDistance getDistanceMeasure() {
		return this.distanceMeasure;
	}

	@Override
	public NearestNeighborLearningAlgorithm getLearningAlgorithm(final TimeSeriesDataset dataset) {
		return new NearestNeighborLearningAlgorithm(ConfigCache.getOrCreate(IAlgorithmConfig.class), this, dataset);
	}
}
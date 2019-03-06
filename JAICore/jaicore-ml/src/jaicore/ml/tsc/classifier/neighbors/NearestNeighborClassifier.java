package jaicore.ml.tsc.classifier.neighbors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.classifier.ASimplifiedTSClassifier;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.distances.ITimeSeriesDistance;

/**
 * NearestNeighborClassifier
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
        public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2) {
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

    /** Values. Set by algorithm. */
    private double[][] values;
    private double[][] timestamps;
    private int[] targets;

    // Constructors.

    public NearestNeighborClassifier(int k, ITimeSeriesDistance distanceMeasure, VoteType voteType) {
        super(new NearestNeighborAlgorithm());

        // Parameter checks.
        if (distanceMeasure == null)
            throw new IllegalArgumentException("Distance measure must not be null");
        if (voteType == null)
            throw new IllegalArgumentException("Vote type must not be null.");

        // Set attributes.
        this.distanceMeasure = distanceMeasure;
        this.k = k;
        this.voteType = voteType;
    }

    public NearestNeighborClassifier(int k, ITimeSeriesDistance distanceMeasure) {
        this(k, distanceMeasure, VoteType.MAJORITY);
    }

    public NearestNeighborClassifier(ITimeSeriesDistance distanceMeasure) {
        this(1, distanceMeasure, VoteType.MAJORITY);
    }

    // Main methods.

    /**
     * Determine the k nearest neighbors for a test instance.
     * 
     * @param testInstance The time series to determine the k nearest neighbors for.
     * @return Queue of the k nearest neighbors as pairs (class, distance).
     */
    protected PriorityQueue<Pair<Integer, Double>> calculateNearestNeigbors(double[] testInstance) {
        int numberOfTrainInstances = values.length;
        // Priority queue of (class, distance)-pairs for nearest neigbors, sorted by
        // distance ascending.
        PriorityQueue<Pair<Integer, Double>> nearestNeighbors = new PriorityQueue<>(nearestNeighborComparator);

        // Calculate the k nearest neighbors.
        for (int i = 0; i < numberOfTrainInstances; i++) {
            double d = distanceMeasure.distance(testInstance, values[i]);

            Pair<Integer, Double> neighbor = new Pair<>(targets[i], d);
            nearestNeighbors.add(neighbor);
            if (nearestNeighbors.size() > k)
                nearestNeighbors.poll();
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
    protected int vote(PriorityQueue<Pair<Integer, Double>> nearestNeighbors) {
        switch (voteType) {
        case WEIGHTED_STEPWISE:
            return voteWeightedStepwise(nearestNeighbors);
        case WEIGHTED_PROPORTIONAL_TO_DISTANCE:
            return voteWeightedProportionalToDistance(nearestNeighbors);
        case MAJORITY:
        default:
            return voteMajority(nearestNeighbors);
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
    protected int voteWeightedStepwise(PriorityQueue<Pair<Integer, Double>> nearestNeighbors) {
        // Voting.
        System.out.print("Hello");
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
    protected int voteWeightedProportionalToDistance(PriorityQueue<Pair<Integer, Double>> nearestNeighbors) {
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
    protected int voteMajority(PriorityQueue<Pair<Integer, Double>> nearestNeighbors) {
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
     * Calculates predicition on a single test instance.
     * 
     * @param testInstance The test instance (not null assured within class).
     * @return
     */
    protected int calculatePrediction(double[] testInstance) {
        // Determine the k nearest neighbors for the test instance.
        PriorityQueue<Pair<Integer, Double>> nearestNeighbors = calculateNearestNeigbors(testInstance);
        // Vote on determined neighbors to create prediction and return prediction.
        int prediction = vote(nearestNeighbors);
        return prediction;
    }

    // Inherited methods.

    @Override
    public Integer predict(double[] univInstance) throws PredictionException {
        if (univInstance == null) {
            throw new IllegalArgumentException("Instance to predict must not be null.");
        }
        return calculatePrediction(univInstance);
    }

    @Override
    public Integer predict(List<double[]> multivInstance) throws PredictionException {
        throw new PredictionException("Can't predict on multivariate data yet.");
    }

    @Override
    public List<Integer> predict(TimeSeriesDataset dataset) throws PredictionException {
        // Parameter checks.
        if (dataset == null)
            throw new IllegalArgumentException("Dataset must not be null.");
        double[][] testInstances = dataset.getValuesOrNull(0);
        if (testInstances == null) {
            throw new PredictionException("Can't predict on empty dataset.");
        }
        // Calculate predictions.
        ArrayList<Integer> predictions = new ArrayList<>(dataset.getNumberOfInstances());
        for (double[] testInstance : testInstances) {
            int prediction = calculatePrediction(testInstance);
            predictions.add(prediction);
        }
        return predictions;
    }

    // Getter and setter.

    protected void setValues(double[][] values) {
        if (values == null)
            throw new IllegalArgumentException("Values must not be null");
        this.values = values;
    }

    protected void setTimestamps(double[][] timestamps) {
        this.timestamps = timestamps;
    }

    protected void setTargets(int[] targets) {
        if (targets == null)
            throw new IllegalArgumentException("Targets must not be null");
        this.targets = targets;
    }

    public int getK() {
        return k;
    }

    public VoteType getVoteType() {
        return voteType;
    }

}
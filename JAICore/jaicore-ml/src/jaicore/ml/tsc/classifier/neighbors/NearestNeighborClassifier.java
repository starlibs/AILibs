package jaicore.ml.tsc.classifier.neighbors;

import java.util.ArrayList;
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

    public enum VoteType {
        MAJORITY, WEIGHTED_STEPWISE, WEIGHTED_PROPORTIONAL_TO_DISTANCE,
    }

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

    private int nearestNeigbor(double[] testInstance) {
        int numberOfTrainInstances = values.length;

        // Priority queue of (class, distance)-pairs for nearest neigbors, sorted by
        // distance ascending.
        PriorityQueue<Pair<Integer, Double>> nearestNeighbors = new PriorityQueue<>(k + 1, (a, b) -> {
            return -1 * a.getY().compareTo(b.getY());
        });

        // Calculate the k nearest neighbors.
        for (int i = 0; i < numberOfTrainInstances; i++) {
            double d = distanceMeasure.distance(testInstance, values[i]);

            Pair<Integer, Double> neighbor = new Pair<>(targets[i], d);
            nearestNeighbors.add(neighbor);
            if (nearestNeighbors.size() > k)
                nearestNeighbors.poll();
        }

        // Vote and return prediction.
        int prediction = vote(nearestNeighbors);

        return prediction;
    }

    /**
     * Performs a vote on the nearest neighbors found. Delegates the vote according
     * to the vote type.
     * 
     * @param nearestNeighbors Priority queue of (class, distance)-pairs for nearest
     *                         neigbors, sorted by distance ascending.
     * @return Result of the vote, i.e. the predicted class.
     */
    private int vote(PriorityQueue<Pair<Integer, Double>> nearestNeighbors) {
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
     *                         neigbors, sorted by distance ascending.
     * @return Result of the vote, i.e. the predicted class.
     */
    private int voteWeightedStepwise(PriorityQueue<Pair<Integer, Double>> nearestNeighbors) {
        return 0;
    }

    /**
     * Performs a vote with weights proportional to the distance on the set nearest
     * neighbors found.
     * 
     * @param nearestNeighbors Priority queue of (class, distance)-pairs for nearest
     *                         neigbors, sorted by distance ascending.
     * @return Result of the vote, i.e. the predicted class.
     */
    private int voteWeightedProportionalToDistance(PriorityQueue<Pair<Integer, Double>> nearestNeighbors) {
        return 0;
    }

    /**
     * Performs a majority vote on the set nearest neighbors found.
     * 
     * @param nearestNeighbors Priority queue of (class, distance)-pairs for nearest
     *                         neigbors, sorted by distance ascending.
     * @return Result of the vote, i.e. the predicted class.
     */
    private int voteMajority(PriorityQueue<Pair<Integer, Double>> nearestNeighbors) {
        // Voting.
        HashMap<Integer, Integer> votes = new HashMap<>();
        for (Pair<Integer, Double> neighbor : nearestNeighbors) {
            Integer target = neighbor.getX();
            Integer currentVotes = votes.get(target);
            if (currentVotes == null) {
                votes.put(target, 0);
            } else {
                votes.put(target, currentVotes + 1);
            }
        }
        // Return most voted target.
        Integer maxNumberOfVotes = Integer.MIN_VALUE;
        Integer mostVotedTarget = -1;
        for (Integer target : votes.keySet()) {
            Integer numberOfVotes = votes.get(target);
            if (numberOfVotes > maxNumberOfVotes) {
                maxNumberOfVotes = numberOfVotes;
                mostVotedTarget = target;
            }
        }
        return mostVotedTarget;
    }

    // Inherited methods.

    @Override
    public Integer predict(double[] univInstance) throws PredictionException {
        return nearestNeigbor(univInstance);
    }

    @Override
    public Integer predict(List<double[]> multivInstance) throws PredictionException {
        throw new PredictionException("Can't predict on multivariate data yet.");
    }

    @Override
    public List<Integer> predict(TimeSeriesDataset dataset) throws PredictionException {
        ArrayList<Integer> predictions = new ArrayList<>(dataset.getNumberOfInstances());
        double[][] testInstances = dataset.getValues(0);
        for (double[] testInstance : testInstances) {
            int prediction = predict(testInstance);
            predictions.add(prediction);
        }
        return predictions;
    }

    // Getter and setter.

    protected void setValues(double[][] values) {
        this.values = values;
    }

    protected void setTimestamps(double[][] timestamps) {
        this.timestamps = timestamps;
    }

    protected void setTargets(int[] targets) {
        this.targets = targets;
    }

    public int getK() {
        return k;
    }

    public VoteType getVoteType() {
        return voteType;
    }

}
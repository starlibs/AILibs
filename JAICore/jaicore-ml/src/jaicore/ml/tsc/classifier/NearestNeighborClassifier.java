package jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.distances.ITimeSeriesDistance;

/**
 * NearestNeighborClassifier
 */
public class NearestNeighborClassifier extends ASimplifiedTSClassifier<Integer> {

    private int k;
    private ITimeSeriesDistance distanceMeasure;

    private double[][] values;
    private int[] targets;
    private double[][] timestamps;

    public NearestNeighborClassifier(int k, ITimeSeriesDistance distanceMeasure) {
        super(new NearestNeighborAlgorithm());
        this.distanceMeasure = distanceMeasure;
        this.k = k;
    }

    protected void setValues(double[][] values) {
        this.values = values;
    }

    protected void setTimestamps(double[][] timestamps) {
        this.timestamps = timestamps;
    }

    protected void setTargets(int[] targets) {
        this.targets = targets;
    }

    private int nearestNeigbor(double[] testInstance) {
        int numberOfTrainInstances = values.length;

        // Priority queue for nearest neigbors, sorted by distance ascending.
        PriorityQueue<Pair<Integer, Double>> nearestNeighbors = new PriorityQueue<>(k + 1, (a, b) -> {
            return -1 * a.getY().compareTo(b.getY());
        });

        // double minimalDistance = Double.MAX_VALUE;
        // int predictedClass = -1;
        for (int i = 0; i < numberOfTrainInstances; i++) {
            double d = distanceMeasure.distance(testInstance, values[i]);

            Pair<Integer, Double> neighbor = new Pair<>(targets[i], d);
            nearestNeighbors.add(neighbor);
            if (nearestNeighbors.size() > k)
                nearestNeighbors.poll();
            // if (d < minimalDistance) {
            // minimalDistance = d;
            // predictedClass = targets[i];
            // }
        }

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

}
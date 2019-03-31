package jaicore.ml.tsc.classifier.neighbors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.tsc.classifier.ASimplifiedTSCAlgorithm;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.distances.ShotgunDistance;

/**
 * Implementation of Shotgun Ensemble Algorihm as published in "Towards Time
 * Series Classfication without Human Preprocessing" by Patrick Schäfer (2014).
 * 
 * Given a maximal window length <code>maxWindowLength</code> and a minumum
 * window length <code>minWindowLength</code>, the Shotgun Ensemble algorithm
 * determines for each of the window lengths form <code>maxWindowLength</code>
 * downto <code>minWindowLength</code> the number of correct predicitions on the
 * training data using the leave-one-out technique.
 * 
 * @author fischor
 */
public class ShotgunEnsembleAlgorithm extends ASimplifiedTSCAlgorithm<Integer, ShotgunEnsembleClassifier> {

    private int minWindowLength;
    private int maxWindowLength;
    private boolean meanNormalization;

    public ShotgunEnsembleAlgorithm(int minWindowLength, int maxWindowLength, boolean meanNormalization) {
        if (minWindowLength < 1) {
            throw new IllegalArgumentException("The parameter minWindowLength must be greater equal to 1.");
        }
        if (maxWindowLength < 1) {
            throw new IllegalArgumentException("The parameter maxWindowLength must be greater equal to 1.");
        }
        if (minWindowLength > maxWindowLength) {
            throw new IllegalAccessError(
                    "The parameter maxWindowsLength must be greater equal to parameter minWindowLength");
        }
        this.minWindowLength = minWindowLength;
        this.maxWindowLength = maxWindowLength;
        this.meanNormalization = meanNormalization;
    }

    @Override
    public void registerListener(Object listener) {

    }

    @Override
    public int getNumCPUs() {
        return 0;
    }

    @Override
    public void setNumCPUs(int numberOfCPUs) {

    }

    @Override
    public void setTimeout(long timeout, TimeUnit timeUnit) {

    }

    @Override
    public void setTimeout(TimeOut timeout) {

    }

    @Override
    public TimeOut getTimeout() {
        return null;
    }

    @Override
    public AlgorithmEvent nextWithException()
            throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
        return null;
    }

    @Override
    public IAlgorithmConfig getConfig() {
        return null;
    }

    @Override
    public ShotgunEnsembleClassifier call()
            throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
        // Check if model and dataset are set.
        if (this.model == null)
            throw new AlgorithmException("Model not set.");
        TimeSeriesDataset dataset = this.getInput();
        if (dataset == null)
            throw new AlgorithmException("No input data set.");
        if (dataset.isMultivariate())
            throw new UnsupportedOperationException("Multivariate datasets are not supported.");

        // Retrieve data from dataset.
        double[][] values = dataset.getValuesOrNull(0);
        int[] targets = dataset.getTargets();
        // Check data.
        if (values == null)
            throw new AlgorithmException("Empty input data set.");
        if (targets == null)
            throw new AlgorithmException("Empty targets.");

        // Holds pairs of (number of correct predictions, window length).
        ArrayList<Pair<Integer, Integer>> scores = new ArrayList<>();

        for (int windowLength = this.maxWindowLength; windowLength >= this.minWindowLength; windowLength--) {
            int correct = 0;

            // 1-NN with Leave-One-Out CV.
            ShotgunDistance shotgunDistance = new ShotgunDistance(windowLength, this.meanNormalization);
            for (int i = 0; i < values.length; i++) {
                // Predict for i-th instance.
                double minDistance = Double.MAX_VALUE;
                int instanceThatMinimizesDistance = -1;
                for (int j = 0; j < values.length; j++) {
                    if (i != j) {
                        double distance = shotgunDistance.distance(values[i], values[j]);
                        if (distance < minDistance) {
                            minDistance = distance;
                            instanceThatMinimizesDistance = j;
                        }
                    }
                }
                // Check, if Leave-One-Out prediction for i-th was correct.
                if (targets[i] == targets[instanceThatMinimizesDistance]) {
                    correct++;
                }
            }

            scores.add(new Pair<>(correct, windowLength));
        }

        // Update model.
        NearestNeighborClassifier nearestNeighborClassifier = new NearestNeighborClassifier(
                new ShotgunDistance(this.maxWindowLength, this.meanNormalization));
        try {
            nearestNeighborClassifier.train(dataset);
        } catch (Exception e) {
            throw new AlgorithmException("Cant train nearest neighbor classifier.");
        }
        this.model.setWindows(scores);
        this.model.setNearestNeighborClassifier(nearestNeighborClassifier);
        return this.model;
    }

    @Override
    public Iterator<AlgorithmEvent> iterator() {
        return null;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public AlgorithmEvent next() {
        return null;
    }

    @Override
    public void cancel() {

    }

}
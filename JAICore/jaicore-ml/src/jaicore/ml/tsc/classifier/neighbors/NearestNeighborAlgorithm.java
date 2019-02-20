package jaicore.ml.tsc.classifier.neighbors;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.tsc.classifier.ASimplifiedTSCAlgorithm;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;

/**
 * Algorithm that backs all nearest neighbor classifiers. When called, it hands
 * the dataset over to the classifier.
 */
public class NearestNeighborAlgorithm extends ASimplifiedTSCAlgorithm<Integer, NearestNeighborClassifier> {

    @Override
    public NearestNeighborClassifier call()
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
        double[][] timestamps = dataset.getTimestampsOrNull(0);
        int[] targets = dataset.getTargets();
        // Check data.
        if (values == null)
            throw new AlgorithmException("Empty input data set.");
        if (targets == null)
            throw new AlgorithmException("Empty targets.");

        // Update model.
        this.model.setValues(values);
        this.model.setTimestamps(timestamps);
        this.model.setTargets(targets);

        return this.model;
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
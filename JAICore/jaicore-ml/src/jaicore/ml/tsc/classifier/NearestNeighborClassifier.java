package jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.List;

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

    public int nearestNeigbor(double[] testInstance) {
        int numberOfTrainInstances = values.length;

        double minimalDistance = Double.MAX_VALUE;
        int predictedClass = -1;
        for (int i = 0; i < numberOfTrainInstances; i++) {
            double d = distanceMeasure.distance(testInstance, values[i]);
            if (d < minimalDistance) {
                minimalDistance = d;
                predictedClass = targets[i];
            }
        }
        return predictedClass;
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
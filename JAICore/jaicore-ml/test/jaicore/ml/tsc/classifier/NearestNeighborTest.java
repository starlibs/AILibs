package jaicore.ml.tsc.classifier;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.distances.EuclideanDistance;

/**
 * NearestNeighborTest
 */
public class NearestNeighborTest {

    TimeSeriesDataset dataset;

    @Before
    public void setUp() {
        double data[][] = { { 0.4, 0.5 }, { 0.4, 0.6 }, { 0.4, 0.4 }, { 0.7, 0.6 }, { 0.7, 0.3 } };
        int[] targets = { 1, 2, 2, 1, 1 };
        ArrayList<double[][]> values = new ArrayList<>(1);
        values.add(data);
        dataset = new TimeSeriesDataset(values, targets);
    }

    @Test
    public void testPredictionWithK1() throws TrainingException, PredictionException {
        int k = 1;
        NearestNeighborClassifier classifier = new NearestNeighborClassifier(k, new EuclideanDistance());
        classifier.train(dataset);
        // Predict.
        double[] instance = { 0.5, 0.5 };
        int prediction = classifier.predict(instance);
        int expectation = 1;
        assertEquals(expectation, prediction);
    }

    @Test
    public void testPredictionWithK2() throws TrainingException, PredictionException {
        int k = 3;
        NearestNeighborClassifier classifier = new NearestNeighborClassifier(k, new EuclideanDistance());
        classifier.train(dataset);
        // Predict.
        double[] instance = { 0.5, 0.5 };
        int prediction = classifier.predict(instance);
        int expectation = 2;
        assertEquals(expectation, prediction);

    }

    @Test
    public void testPredictionWithK5() throws TrainingException, PredictionException {
        int k = 5;
        NearestNeighborClassifier classifier = new NearestNeighborClassifier(k, new EuclideanDistance());
        classifier.train(dataset);
        // Predict.
        double[] instance = { 0.5, 0.5 };
        int prediction = classifier.predict(instance);
        int expectation = 1;
        assertEquals(expectation, prediction);
    }

}
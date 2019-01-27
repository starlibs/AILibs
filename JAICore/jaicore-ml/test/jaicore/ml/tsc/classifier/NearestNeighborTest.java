package jaicore.ml.tsc.classifier;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.core.exception.PredictionException;
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
    public void testPredictionWithK1() {
        int k = 1;
        NearestNeighborAlgorithm algorithm = new NearestNeighborAlgorithm();
        NearestNeighborClassifier classifier = new NearestNeighborClassifier(k, new EuclideanDistance());

        algorithm.setModel(classifier);
        algorithm.setInput(dataset);

        try {
            algorithm.call();
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (AlgorithmExecutionCanceledException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (TimeoutException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (AlgorithmException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        double[] instance = { 0.5, 0.5 };
        int prediction;
        try {
            prediction = classifier.predict(instance);
            int expectation = 1;
            assertEquals(expectation, prediction);
        } catch (PredictionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testPredictionWithK2() {
        int k = 3;
        NearestNeighborAlgorithm algorithm = new NearestNeighborAlgorithm();
        NearestNeighborClassifier classifier = new NearestNeighborClassifier(k, new EuclideanDistance());

        algorithm.setModel(classifier);
        algorithm.setInput(dataset);

        try {
            algorithm.call();
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (AlgorithmExecutionCanceledException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (TimeoutException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (AlgorithmException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        double[] instance = { 0.5, 0.5 };
        int prediction;
        try {
            prediction = classifier.predict(instance);
            int expectation = 2;
            assertEquals(expectation, prediction);
        } catch (PredictionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testPredictionWithK5() {
        int k = 5;
        NearestNeighborAlgorithm algorithm = new NearestNeighborAlgorithm();
        NearestNeighborClassifier classifier = new NearestNeighborClassifier(k, new EuclideanDistance());

        algorithm.setModel(classifier);
        algorithm.setInput(dataset);

        try {
            algorithm.call();
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (AlgorithmExecutionCanceledException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (TimeoutException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (AlgorithmException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        double[] instance = { 0.5, 0.5 };
        int prediction;
        try {
            prediction = classifier.predict(instance);
            int expectation = 1;
            assertEquals(expectation, prediction);
        } catch (PredictionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
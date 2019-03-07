package jaicore.ml.tsc.classifier.neighbors;

import java.util.ArrayList;
import java.util.PriorityQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Before;
import org.junit.Test;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.distances.EuclideanDistance;

/**
 * Test suite for the
 * {@link jaicore.ml.tsc.classifier.neighbors.NearestNeighborClassifer}
 * implementation.
 */
public class NearestNeighborTest {

    /**
     * Dataset containing the values <code>
     * { 
     *  { 0.4, 0.5 }, 
     *  { 0.4, 0.6 }, 
     *  { 0.4, 0.4 }, 
     *  { 0.7, 0.6 }, 
     *  { 0.7, 0.3 } 
     * }
     * </code> after set up.
     */
    TimeSeriesDataset dataset;

    /**
     * Priority queue containing the (targetClass, distance)-pairs <code> 
     * { 
     *  (1, 0.1),
     *  (2, 0.2), 
     *  (3, 0.3), 
     *  (2, 0.4), 
     *  (3, 0.8), 
     *  (3, 1.6)
     * }</code>.
     */
    PriorityQueue<Pair<Integer, Double>> nearestNeighbors;

    @Before
    public void setUp() {
        // Set up dataset.
        double data[][] = { { 0.4, 0.5 }, { 0.4, 0.6 }, { 0.4, 0.4 }, { 0.7, 0.6 }, { 0.7, 0.3 } };
        int[] targets = { 1, 2, 2, 1, 1 };
        ArrayList<double[][]> values = new ArrayList<>(1);
        values.add(data);
        dataset = new TimeSeriesDataset(values, targets);

        // Set up priority queue.
        nearestNeighbors = new PriorityQueue<>(NearestNeighborClassifier.nearestNeighborComparator);
        nearestNeighbors.add(new Pair<Integer, Double>(1, 0.1));
        nearestNeighbors.add(new Pair<Integer, Double>(2, 0.2));
        nearestNeighbors.add(new Pair<Integer, Double>(3, 0.3));
        nearestNeighbors.add(new Pair<Integer, Double>(2, 0.4));
        nearestNeighbors.add(new Pair<Integer, Double>(3, 0.8));
        nearestNeighbors.add(new Pair<Integer, Double>(3, 1.6));
    }

    /**
     * Create prediction for the test instance <code>t = { 0.5, 0.5 }</code> on the
     * dataset with <code>k=1</code> and euclidean distance. Since the nearest
     * neighbor of <code>t</code> is <code>s = {0.4, 0.5}</code> the predicition
     * should yield the class of <code>s</code>, that is <code>1</code>.
     * 
     * @throws TrainingException
     * @throws PredictionException
     */
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

    /**
     * Create prediction for the test instance <code>t = { 0.5, 0.5 }</code> on the
     * dataset with <code>k=3</code>, euclidean distance and majority vote. Since
     * the three nearest neighbors of <code>t</code> are <code>
     * s = {0.4, 0.5},
     * t = {0.4, 0.4},
     * u = {0.4, 0.6}
     * </code> the predicition should be the majority class of the classes of
     * <code>s, t, u</code>, that is the majority of <code>1, 2, 2</code>, that is
     * <code>2</code>.
     * 
     * @throws TrainingException
     * @throws PredictionException
     */
    @Test
    public void testPredictionWithK3() throws TrainingException, PredictionException {
        int k = 3;
        NearestNeighborClassifier classifier = new NearestNeighborClassifier(k, new EuclideanDistance());
        classifier.train(dataset);
        // Predict.
        double[] instance = { 0.5, 0.5 };
        int prediction = classifier.predict(instance);
        int expectation = 2;
        assertEquals(expectation, prediction);

    }

    /**
     * Create prediction for the test instance <code>t = { 0.5, 0.5 }</code> on the
     * dataset with <code>k=5</code>, euclidean distance and majority vote. Since
     * the five nearest neighbors of <code>t</code> are all the instances in the
     * dataset, the predicition should be the majority class of the classes of the
     * dataset instances, that is the majority of <code>1, 2, 2, 1, 1</code>, that
     * is <code>1</code>.
     * 
     * @throws TrainingException
     * @throws PredictionException
     */
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

    /**
     * Test the @see NearestNeighborComparator by first adding elements to a
     * priority queue using the comparator and then polling the elements out and
     * check the sequence of polled elements.
     */
    @Test
    public void testNeirestNeighborComparator() {
        // Create priority queue and fill (in not sorted order).
        PriorityQueue<Pair<Integer, Double>> queue = new PriorityQueue<>(
                NearestNeighborClassifier.nearestNeighborComparator);
        nearestNeighbors.add(new Pair<Integer, Double>(3, 1.6));
        nearestNeighbors.add(new Pair<Integer, Double>(3, 0.3));
        nearestNeighbors.add(new Pair<Integer, Double>(1, 0.1));
        nearestNeighbors.add(new Pair<Integer, Double>(3, 0.8));
        nearestNeighbors.add(new Pair<Integer, Double>(2, 0.2));
        nearestNeighbors.add(new Pair<Integer, Double>(2, 0.4));

        // Poll every element and assure correct sequence of polled elements.
        Pair<Integer, Double> pair;
        pair = queue.poll();
        assertEquals(1.6, (double) pair.getY(), .0);
        pair = queue.poll();
        assertEquals(0.8, (double) pair.getY(), .0);
        pair = queue.poll();
        assertEquals(0.4, (double) pair.getY(), .0);
        pair = queue.poll();
        assertEquals(0.3, (double) pair.getY(), .0);
        pair = queue.poll();
        assertEquals(0.2, (double) pair.getY(), .0);
        pair = queue.poll();
        assertEquals(0.1, (double) pair.getY(), .0);
    }

    /**
     * Tests the majority vote. Since he nearest neighbors priority queue contains
     * the elements with the classes <code>1, 2, 3, 2, 3, 3</code> the expected
     * result is <code>3</code>.
     */
    @Test
    public void testVoteMajority() {
        NearestNeighborClassifier classifier = new NearestNeighborClassifier(6, new EuclideanDistance());
        int votedTargetClass = classifier.voteMajority(nearestNeighbors);
        int expectation = 3;
        assertEquals(expectation, votedTargetClass);
    }

    /**
     * Tests the weighted stepwise vote. The nearest neighbors priority queue
     * contains the elements with the classes <code>1, 2, 3, 2, 3, 3</code> (in
     * order). Thus,the weight for each class is <code>
     * class 1: 6,
     * class 2: 8,
     * class 3: 7</code>. Thus, the expected result should be class <code>2</code>.
     */
    @Test
    public void testVoteWeightedStepwise() {
        NearestNeighborClassifier classifier = new NearestNeighborClassifier(6, new EuclideanDistance());
        int votedTargetClass = classifier.voteWeightedStepwise(nearestNeighbors);
        int expectation = 2;
        assertEquals(expectation, votedTargetClass);
    }

    /**
     * Tests the weighted proportional to distance vote. The elements of the nearest
     * neighbors priority queue and its weight <code>w_i</code> are <code> 
     * { 
     *  (1, 0.1), w_1 = 10;
     *  (2, 0.2), w_2 = 5;
     *  (3, 0.3), w_3 = 3.3;
     *  (2, 0.4), w_4 = 2.5;
     *  (3, 0.8), w_5 = 1.25;
     *  (3, 1.6), w_6 = 0.625;
     * }</code>. Thus the weights for each class are <code>
     * class 1: 10,
     * class 2: 7.5,
     * class 3: 5.175.</code> Thus, the expected result shuld be class
     * <code>1</code>.
     */
    @Test
    public void testVoteWeightedProportionalToDistance() {
        NearestNeighborClassifier classifier = new NearestNeighborClassifier(6, new EuclideanDistance());
        int votedTargetClass = classifier.voteWeightedProportionalToDistance(nearestNeighbors);
        int expectation = 1;
        assertEquals(expectation, votedTargetClass);
    }

    /**
     * Tests if IllegalArgumetnExceptions are thrown when making calls with
     * <code>null</code> objects.
     */
    @Test
    public void testPredictionWithNullInstanceThrowsIllegalArgumentException() {
        // For single instance prediciton.
        assertThrows(IllegalArgumentException.class, () -> {
            NearestNeighborClassifier classifier = new NearestNeighborClassifier(6, new EuclideanDistance());
            classifier.train(dataset);
            classifier.predict((double[]) null);
        });
        // For prediciton on dataset.
        assertThrows(IllegalArgumentException.class, () -> {
            NearestNeighborClassifier classifier = new NearestNeighborClassifier(6, new EuclideanDistance());
            classifier.train(dataset);
            classifier.predict((TimeSeriesDataset) null);
        });
    }

}
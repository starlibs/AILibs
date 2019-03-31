package jaicore.ml.tsc.classifier.neighbors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;

/**
 * Test suite for the
 * {@link jaicore.ml.tsc.classifier.neighbors.ShotgunEnsembleAlgorithm}
 * implementation.
 * 
 * @author fischor
 */
public class ShotgunEnsembleAlgorithmTest {

    /**
     * Dataset containing the values <code>
     * { 
     *  { 0.1, 0.1, 0.8, 0.1 },
     *  { 0.25, 0.2, 0.25, 0.2 },
     *  { 0.1, 0.2, 0.3, 0.5 },
     *  { 0.15, 0.14, 0.1, 0.1 }
     * }
     * </code> after set up.
     */
    TimeSeriesDataset dataset;

    ShotgunEnsembleClassifier model;

    ShotgunEnsembleAlgorithm algorithm;

    @Before
    public void setUp() {
        // Set up dataset.
        double data[][] = { { 0.1, 0.1, 0.8, 0.1 }, { 0.25, 0.2, 0.25, 0.2 }, { 0.1, 0.2, 0.3, 0.5 },
                { 0.15, 0.14, 0.1, 0.1 } };
        int[] targets = { 1, 2, 1, 2 };
        ArrayList<double[][]> values = new ArrayList<>(1);
        values.add(data);
        dataset = new TimeSeriesDataset(values, targets);

        // Create algorithm
        int minWindowLength = 4;
        int maxWindowLength = 6;
        boolean meanNormalization = true;
        this.algorithm = new ShotgunEnsembleAlgorithm(minWindowLength, maxWindowLength, meanNormalization);

        // Create model.
        double factor = 1;
        this.model = new ShotgunEnsembleClassifier(algorithm, factor);
    }

    @Test
    public void testCorrectness()
            throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
        // Create algorithm.
        int minWindowLength = 3;
        int maxWindowLength = 4;
        boolean meanNormalization = true;
        ShotgunEnsembleAlgorithm algorithm = new ShotgunEnsembleAlgorithm(minWindowLength, maxWindowLength,
                meanNormalization);

        // Create model.
        double factor = 1;
        ShotgunEnsembleClassifier model = new ShotgunEnsembleClassifier(algorithm, factor);

        // Training.
        algorithm.setInput(this.dataset);
        algorithm.setModel(model);
        algorithm.call();

        // Check model to contains (3, 2) and (4, 2).
        assertEquals(2, model.windows.size());
        for (Pair<Integer, Integer> window : model.windows) {
            switch (window.getY()) {
            case 3:
                assertEquals(2, (int) window.getX());
                break;
            case 4:
                assertEquals(2, (int) window.getX());
                break;
            default:
                assertEquals(1, 2);
            }
        }
    }

    @Test
    public void testRobustnessForConstructorWithInvalidWindowLenghts1() {
        // Too low minWindowLength.
        assertThrows(IllegalArgumentException.class, () -> {
            int minWindowLength = 0;
            int maxWindowLength = 3;
            new ShotgunEnsembleAlgorithm(minWindowLength, maxWindowLength, true);
        });
    }

    @Test
    public void testRobustnessForConstructorWithInvalidWindowLenghts2() {
        // Too low maxWindowLength.
        assertThrows(IllegalArgumentException.class, () -> {
            int minWindowLength = 3;
            int maxWindowLength = 0;
            new ShotgunEnsembleAlgorithm(minWindowLength, maxWindowLength, true);
        });
    }

    @Test
    public void testRobustnessForConstructorWithInvalidWindowLenghts3() {
        // Too low maxWindowLength.
        assertThrows(IllegalArgumentException.class, () -> {
            int minWindowLength = 3;
            int maxWindowLength = 0;
            new ShotgunEnsembleAlgorithm(minWindowLength, maxWindowLength, true);
        });
    }

    @Test
    public void testRobustnessForCallingWithoutModelSet()
            throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
        this.algorithm.setInput(this.dataset);

        // Call algorithm without model set.
        assertThrows(AlgorithmException.class, () -> {
            algorithm.call();
        });
    }

    @Test
    public void testRobustnessForCallingWithoutDatasetSet()
            throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
        this.algorithm.setModel(this.model);

        // Call algorithm without model set.
        assertThrows(AlgorithmException.class, () -> {
            algorithm.call();
        });
    }

}
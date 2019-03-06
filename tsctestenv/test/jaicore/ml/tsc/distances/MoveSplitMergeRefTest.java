package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.util.ClassMapper;
import jaicore.ml.tsc.util.SimplifiedTimeSeriesLoader;
import jaicore.ml.tsc.util.TimeSeriesUtil;
import timeseriesweka.elastic_distance_measures.MSMDistance;

public class MoveSplitMergeRefTest {

    /** Local path for the datasets arff files. */
    private static final String PATH = "./tsctestenv/data/univariate/";

    /** Path for pen digits dataset. */
    private static final String CAR = PATH + "PenDigits/PenDigitsDimension1_TEST.arff";

    /** Dataset used for comparison tests. */
    private TimeSeriesDataset dataset;

    /**
     * Load the dataset.
     * 
     * @throws TimeSeriesLoadingException
     */
    @Before
    public void setUp() throws TimeSeriesLoadingException {
        // Load dataset.
        File arffFile = new File(CAR);
        Pair<TimeSeriesDataset, ClassMapper> trainPair = SimplifiedTimeSeriesLoader.loadArff(arffFile);
        dataset = trainPair.getX();
    }

    /**
     * Compares the correctness for the distance calculation on a whole dataset by
     * checking if the implementation and the reference are calculating equal
     * distances.
     */
    @Test
    public void testCorrectness() {
        double cutoff = Double.MAX_VALUE;

        double c = 1;
        MSMDistance referenceMoveSplitMerge = new MSMDistance(c);
        MoveSplitMerge moveSplitMerge = new MoveSplitMerge(c);

        double[][] values = dataset.getValues(0);
        for (int i = 0; i < 100; i++) {
            for (int j = i; j < values.length; j++) {
                double referenceDistance = referenceMoveSplitMerge.distance(values[i], values[j], cutoff);
                double distance = moveSplitMerge.distance(values[i], values[j]);
                String message = String.format("Distance between %s and %s.", TimeSeriesUtil.toString(values[i]),
                        TimeSeriesUtil.toString(values[j]));
                assertEquals(message, referenceDistance, distance, 10e-5);
            }
        }
    }

    /**
     * Compares the performance for the distance calculation on a whole dataset by
     * measuring calculation time.
     */
    @Test
    public void testPerformance() {
        // Get values.
        double[][] values = dataset.getValues(0);
        int numberOfTestInstances = 100;

        double c = 1;

        // Measure time for reference implementation.
        double cutoff = Double.MAX_VALUE;
        double refStart = System.currentTimeMillis();
        MSMDistance referenceMoveSplitMerge = new MSMDistance(c);
        for (int i = 0; i < numberOfTestInstances; i++) {
            for (int j = i; j < values.length; j++) {
                referenceMoveSplitMerge.distance(values[i], values[j], cutoff);
            }
        }
        double refEnd = System.currentTimeMillis();

        // Measure time for own implementation.
        double ownStart = System.currentTimeMillis();
        MoveSplitMerge moveSplitMerge = new MoveSplitMerge(c);
        for (int i = 0; i < numberOfTestInstances; i++) {
            for (int j = i; j < values.length; j++) {
                moveSplitMerge.distance(values[i], values[j]);
            }
        }
        double ownEnd = System.currentTimeMillis();

        // Compare performance.
        double refTime = refEnd - refStart;
        double ownTime = ownEnd - ownStart;
        String message = String.format("Reference implementation was faster. Reference: %.3f ms, own: %.3f ms", refTime,
                ownTime);
        assertTrue(message, ownTime <= refTime);
    }
}
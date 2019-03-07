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
import jaicore.ml.tsc.util.ScalarDistanceUtil;
import jaicore.ml.tsc.util.SimplifiedTimeSeriesLoader;
import jaicore.ml.tsc.util.TimeSeriesUtil;
import timeseriesweka.elastic_distance_measures.WeightedDTW;

/**
 * Tests performance and correctness of the {@link WeightedDynamicTimeWarping}
 * against the reference implementation.
 * 
 * <p>
 * This class provide a method
 * <code>double distance(double[] a, double[] b, double cutoff)</code>, where
 * <code>a</code> and <code>b</code> are the time series to measure the distance
 * for and <code>cutoff</code> is the threshold used for early abandon. This
 * methods use the squared distance <code>d(x,y)=(x-y)*(x-y)</code> as distance
 * between two points <code>x</code> and <code>y</code>.
 * </p>
 */
public class WeightedDynamicTimeWarpingRefTest {

    /** Local path for the datasets arff files. */
    private static final String PATH = "./tsctestenv/data/univariate/";

    /** Path for dataset. */
    private static final String DATASET_PATH = PATH + "PenDigits/PenDigitsDimension1_TEST.arff";

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
        File arffFile = new File(DATASET_PATH);
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
        double g = 1;
        double Wmax = 1;

        WeightedDTW referenceWeightedDynamicTimeWarping = new WeightedDTW(g);
        WeightedDynamicTimeWarping weightedDynamicTimeWarping = new WeightedDynamicTimeWarping(g, Wmax,
                ScalarDistanceUtil.getSquaredDistance());

        double[][] values = dataset.getValues(0);
        for (int i = 0; i < 100; i++) {
            for (int j = i; j < values.length; j++) {
                double referenceDistance = referenceWeightedDynamicTimeWarping.distance(values[i], values[j], cutoff);
                double distance = weightedDynamicTimeWarping.distance(values[i], values[j]);
                String message = String.format("Distance between %s and %s.", TimeSeriesUtil.toString(values[i]),
                        TimeSeriesUtil.toString(values[j]));
                assertEquals(message, referenceDistance, distance, 10e-5);
                System.out.println("Correct");
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

        double cutoff = Double.MAX_VALUE;
        double g = 1;
        double Wmax = 1;

        // Measure time for reference implementation.
        double refStart = System.currentTimeMillis();
        WeightedDTW referenceWeightedDynamicTimeWarping = new WeightedDTW(g);
        for (int i = 0; i < numberOfTestInstances; i++) {
            for (int j = i; j < values.length; j++) {
                referenceWeightedDynamicTimeWarping.distance(values[i], values[j], cutoff);
            }
        }
        double refEnd = System.currentTimeMillis();

        // Measure time for own implementation.
        double ownStart = System.currentTimeMillis();
        WeightedDynamicTimeWarping weightedDynamicTimeWarping = new WeightedDynamicTimeWarping(g, Wmax,
                ScalarDistanceUtil.getSquaredDistance());
        for (int i = 0; i < numberOfTestInstances; i++) {
            for (int j = i; j < values.length; j++) {
                weightedDynamicTimeWarping.distance(values[i], values[j]);
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
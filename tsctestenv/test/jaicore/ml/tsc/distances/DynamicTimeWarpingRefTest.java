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
import timeseriesweka.elastic_distance_measures.BasicDTW;

/**
 * Tests performance and correctness of the {@link DynamicTimeWarping} against
 * the reference implementation.
 * 
 * The reference implementation offers the following classes for Dynamic Time
 * Warping:
 * <p>
 * <ul>
 * <li>DTW.java</li>
 * <li>BasicDTW.java</li>
 * <li>DTW_DistanceBasic.java</li>
 * <li>DTW_DistanceEfficient.java - This implementation is more memory
 * efficient.</li>
 * <li>WeightedDTW.java - Not tested here. See
 * {@link WeightedDynamicTimeWarpingRefTest}.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Basically all these classes provide a method
 * <code>double distance(double[] a, double[] b, double cutoff)</code>, where
 * <code>a</code> and <code>b</code> are the time series to measure the distance
 * for and <code>cutoff</code> is the threshold used for early abandon. All
 * these methods us the squared distance <code>d(x,y)=(x-y)*(x-y)</code> as
 * distance between two points <code>x</code> and <code>y</code>.
 * </p>
 */
public class DynamicTimeWarpingRefTest {

    /** Local path for the datasets arff files. */
    private static final String PATH = "./tsctestenv/data/univariate/";

    /** Path for pen digits dataset. */
    private static final String CAR = PATH + "Car/Car/Car_TRAIN.arff";

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

        BasicDTW referenceDynamicTimeWarping = new BasicDTW();
        DynamicTimeWarping dynamicTimeWarping = new DynamicTimeWarping(ScalarDistanceUtil.getSquaredDistance());

        double[][] values = dataset.getValues(0);
        for (int i = 0; i < 100; i++) {
            for (int j = i; j < values.length; j++) {
                double referenceDistance = referenceDynamicTimeWarping.distance(values[i], values[j], cutoff);
                double distance = dynamicTimeWarping.distance(values[i], values[j]);
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

        // Measure time for reference implementation.
        double cutoff = Double.MAX_VALUE;
        double refStart = System.currentTimeMillis();
        BasicDTW referenceDynamicTimeWarping = new BasicDTW();
        for (int i = 0; i < numberOfTestInstances; i++) {
            for (int j = i; j < values.length; j++) {
                referenceDynamicTimeWarping.distance(values[i], values[j], cutoff);
            }
        }
        double refEnd = System.currentTimeMillis();

        // Measure time for own implementation.
        double ownStart = System.currentTimeMillis();
        DynamicTimeWarping dynamicTimeWarping = new DynamicTimeWarping(ScalarDistanceUtil.getSquaredDistance());
        for (int i = 0; i < numberOfTestInstances; i++) {
            for (int j = i; j < values.length; j++) {
                dynamicTimeWarping.distance(values[i], values[j]);
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
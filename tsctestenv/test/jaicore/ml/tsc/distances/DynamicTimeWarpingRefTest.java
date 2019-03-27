package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

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
import weka.classifiers.lazy.kNN;

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

    @Test
    public void testCorrectnessForDistanceCalculationOnCarDataset() throws IOException, TimeSeriesLoadingException {
        double delta = 0.001;
        File arffFile = DistanceRefTestUtil.getCarArffFile();

        weka.core.EuclideanDistance referenceImplementation = this.getReferenceDynamicTimeWarping();
        ITimeSeriesDistance ownImplementation = new DynamicTimeWarping(ScalarDistanceUtil.getSquaredDistance());

        String result = DistanceRefTestUtil.runCorrectnessTest(referenceImplementation, ownImplementation, arffFile,
                delta);

        assertNull(result, result);
    }

    @Test
    public void testPerformanceForDistanceCalculationOnCarDataset() throws IOException, TimeSeriesLoadingException {
        File arffFile = DistanceRefTestUtil.getCarArffFile();

        weka.core.EuclideanDistance referenceImplementation = this.getReferenceDynamicTimeWarping();
        ITimeSeriesDistance ownImplementation = new DynamicTimeWarping(ScalarDistanceUtil.getSquaredDistance());

        Pair<Double, Double> times = DistanceRefTestUtil.runPerformanceTest(referenceImplementation, ownImplementation,
                arffFile);
        double refTime = times.getX();
        double ownTime = times.getY();
        String message = String.format("Reference implementation was faster. Reference: %.3f ms, own: %.3f ms", refTime,
                ownTime);

        System.out.println(String.format("Reference: %.3f ms, own: %.3f ms", refTime, ownTime));
        assertTrue(message, refTime > ownTime);
    }

    public weka.core.EuclideanDistance getReferenceDynamicTimeWarping() {
        // A simple DTW algorithm that computes the warped path with no constraints.
        // DTW dtw = new DTW();

        // DTW with early abandon
        BasicDTW referenceDynamicTimeWarping = new BasicDTW(); // Uses Squared Distance as point distance.

        return referenceDynamicTimeWarping;
    }

    public kNN getReferenceNearestNeighborWithDynamicTimeWarping() {
        kNN knn = new kNN();
        weka.core.EuclideanDistance distance = getReferenceDynamicTimeWarping();
        knn.setDistanceFunction(distance);
        return knn;
    }

}
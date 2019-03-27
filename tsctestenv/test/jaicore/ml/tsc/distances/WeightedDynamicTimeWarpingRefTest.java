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
import timeseriesweka.elastic_distance_measures.WeightedDTW;
import weka.classifiers.lazy.kNN;

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

    @Test
    public void testCorrectnessForDistanceCalculationUsingEuclideanDistanceOnCarDataset()
            throws IOException, TimeSeriesLoadingException {
        double g = 1;
        double Wmax = 1;

        double delta = 0.001;
        File arffFile = DistanceRefTestUtil.getCarArffFile();

        weka.core.EuclideanDistance referenceImplementation = getReferenceWeightedDynamicTimeWarping(g, Wmax);
        ITimeSeriesDistance ownImplementation = new WeightedDynamicTimeWarping(g, Wmax,
                ScalarDistanceUtil.getSquaredDistance());

        String result = DistanceRefTestUtil.runCorrectnessTest(referenceImplementation, ownImplementation, arffFile,
                delta);

        assertNull(result, result);
    }

    @Test
    public void testPerformanceForDistanceCalculationUsingDTWOnCarDataset()
            throws IOException, TimeSeriesLoadingException {
        double g = 1;
        double Wmax = 1;

        File arffFile = DistanceRefTestUtil.getCarArffFile();

        weka.core.EuclideanDistance referenceImplementation = getReferenceWeightedDynamicTimeWarping(g, Wmax);
        ITimeSeriesDistance ownImplementation = new WeightedDynamicTimeWarping(g, Wmax,
                ScalarDistanceUtil.getSquaredDistance());

        Pair<Double, Double> times = DistanceRefTestUtil.runPerformanceTest(referenceImplementation, ownImplementation,
                arffFile);
        double refTime = times.getX();
        double ownTime = times.getY();
        String message = String.format("Reference implementation was faster. Reference: %.3f ms, own: %.3f ms", refTime,
                ownTime);

        System.out.println(String.format("Reference: %.3f ms, own: %.3f ms", refTime, ownTime));
        assertTrue(message, refTime > ownTime);
    }

    public weka.core.EuclideanDistance getReferenceWeightedDynamicTimeWarping(double g, double Wmax) {
        return new WeightedDTW(g); // Wmax = 1 fixed
    }

    public kNN getReferenceNearestNeighborWithWeightedDynamicTimeWarping(double g) {
        kNN refClf = new kNN(); // k = 1 by default
        WeightedDTW referenceWeightedDynamicTimeWarping = new WeightedDTW(g); // Wmax = 1 fixed
        refClf.setDistanceFunction(referenceWeightedDynamicTimeWarping);
        return refClf;
    }
}
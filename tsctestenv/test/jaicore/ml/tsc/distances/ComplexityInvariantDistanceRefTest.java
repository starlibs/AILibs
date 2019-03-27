package jaicore.ml.tsc.distances;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.tsc.complexity.SquaredBackwardDifferenceComplexity;
import jaicore.ml.tsc.complexity.StretchingComplexity;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.util.ScalarDistanceUtil;
import timeseriesweka.classifiers.NN_CID.CIDDistance;
import weka.classifiers.lazy.kNN;
import timeseriesweka.classifiers.NN_CID;
import timeseriesweka.classifiers.NN_CID.CIDDTWDistance;

/**
 * ComplexityInvariantDistanceRefTest
 */
public class ComplexityInvariantDistanceRefTest {

    /**
     * Checks correctness for distance calulcation on a whole dataset by comparing
     * calulated distances.
     */
    @Test
    public void testCorrectnessForDistanceCalculationUsingEuclideanDistanceOnCarDataset()
            throws IOException, TimeSeriesLoadingException {
        double delta = 0.001;
        File arffFile = DistanceRefTestUtil.getCarArffFile();

        weka.core.EuclideanDistance referenceImplementation = this.getReferenceComplexityInvariantDistance(false);
        ITimeSeriesDistance ownImplementation = new ComplexityInvariantDistance(new EuclideanDistance(),
                new SquaredBackwardDifferenceComplexity());
        String result = DistanceRefTestUtil.runCorrectnessTest(referenceImplementation, ownImplementation, arffFile,
                delta);

        assertNull(result, result);

        // In their code, their square root twice for the Euclidean Distance (in line
        // 199 and 211). They also add some delta (in line 20 and 204) to the complexity
        // estimates. Therefore, their code is assumed to be incorrect. No comparison
        // made here.
    }

    /**
     * Checks correctness for distance calulcation on a whole dataset by comparing
     * calulated distances.
     */
    @Test
    public void testCorrectnessForDistanceCalculationUsingDTWOnCarDataset()
            throws IOException, TimeSeriesLoadingException {
        double delta = 0.001;
        File arffFile = DistanceRefTestUtil.getCarArffFile();

        weka.core.EuclideanDistance referenceImplementation = this.getReferenceComplexityInvariantDistance(true);
        ITimeSeriesDistance ownImplementation = new ComplexityInvariantDistance(
                new DynamicTimeWarping(ScalarDistanceUtil.getAbsoluteDistance()),
                new SquaredBackwardDifferenceComplexity());
        String result = DistanceRefTestUtil.runCorrectnessTest(referenceImplementation, ownImplementation, arffFile,
                delta);

        assertNull(result, result);

        // Their code is wrong again.
    }

    /**
     * Compares the performance for the distance calculation on a whole dataset by
     * measuring calculation time.
     */
    @Test
    public void testPerformanceForDistanceCalculationUsingEuclideanDistanceOnCarDataset()
            throws IOException, TimeSeriesLoadingException {
        double c = 1;
        File arffFile = DistanceRefTestUtil.getCarArffFile();

        weka.core.EuclideanDistance referenceImplementation = this.getReferenceComplexityInvariantDistance(false);
        ITimeSeriesDistance ownImplementation = new ComplexityInvariantDistance(new EuclideanDistance(),
                new StretchingComplexity());
        Pair<Double, Double> times = DistanceRefTestUtil.runPerformanceTest(referenceImplementation, ownImplementation,
                arffFile);
        double refTime = times.getX();
        double ownTime = times.getY();
        String message = String.format("Reference implementation was faster. Reference: %.3f ms, own: %.3f ms", refTime,
                ownTime);

        System.out.println(String.format("Reference: %.3f ms, own: %.3f ms", refTime, ownTime));
        assertTrue(message, refTime > ownTime);
    }

    /**
     * Compares the performance for the distance calculation on a whole dataset by
     * measuring calculation time.
     */
    @Test
    public void testPerformanceForDistanceCalculationUsingDTWOnCarDataset()
            throws IOException, TimeSeriesLoadingException {
        double c = 1;
        File arffFile = DistanceRefTestUtil.getCarArffFile();

        weka.core.EuclideanDistance referenceImplementation = this.getReferenceComplexityInvariantDistance(true);
        ITimeSeriesDistance ownImplementation = new ComplexityInvariantDistance(
                new DynamicTimeWarping(ScalarDistanceUtil.getAbsoluteDistance()),
                new SquaredBackwardDifferenceComplexity());

        Pair<Double, Double> times = DistanceRefTestUtil.runPerformanceTest(referenceImplementation, ownImplementation,
                arffFile);
        double refTime = times.getX();
        double ownTime = times.getY();
        String message = String.format("Reference implementation was faster. Reference: %.3f ms, own: %.3f ms", refTime,
                ownTime);

        System.out.println(String.format("Reference: %.3f ms, own: %.3f ms", refTime, ownTime));
        assertTrue(message, refTime > ownTime);
    }

    public weka.core.EuclideanDistance getReferenceComplexityInvariantDistance(boolean dtw) {
        // https://bitbucket.org/TonyBagnall/time-series-classification/src/f4fe66b74e039b0475a87ebf6d57db400da25b63/TimeSeriesClassification/src/tsc_algorithms/NN_CID.java
        return dtw ? new CIDDTWDistance() : new CIDDistance();
    }

    public kNN getReferenceNearestNeighborWithCID() {
        NN_CID nnCid = new NN_CID(); // Uses Euclidean Distance.
        // nnCid.useDTW(); // Uses DTW.
        return nnCid;
    }
}
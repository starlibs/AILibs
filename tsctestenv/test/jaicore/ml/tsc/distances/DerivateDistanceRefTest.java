package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.filter.derivate.BackwardDifferenceDerivate;
import jaicore.ml.tsc.util.ScalarDistanceUtil;
import timeseriesweka.classifiers.DD_DTW;
import timeseriesweka.classifiers.DD_DTW.DistanceType;
import timeseriesweka.classifiers.DD_DTW.GoreckiDerivativesEuclideanDistance;
import timeseriesweka.classifiers.DD_DTW.GoreckiDerivativesDTW;

import weka.classifiers.lazy.kNN;

/**
 * DerivateDistanceRefTest
 */
public class DerivateDistanceRefTest {

    @Test
    public void testCorrectnessForDistanceCalculationUsingEuclideanDistanceOnCarDataset()
            throws IOException, TimeSeriesLoadingException {
        double alpha = 0.5;

        double delta = 0.001;
        File arffFile = DistanceRefTestUtil.getCarArffFile();

        weka.core.EuclideanDistance referenceImplementation = this.getReferenceDerivateDistance(alpha, false);
        ITimeSeriesDistance ownImplementation = new DerivateDistance(alpha, new BackwardDifferenceDerivate(),
                new EuclideanDistance());

        String result = DistanceRefTestUtil.runCorrectnessTest(referenceImplementation, ownImplementation, arffFile,
                delta);

        assertNull(result, result);
    }

    @Test
    public void testCorrectnessForDistanceCalculationUsingDTWOnCarDataset()
            throws IOException, TimeSeriesLoadingException {
        double alpha = 0.5;

        double delta = 0.001;
        File arffFile = DistanceRefTestUtil.getCarArffFile();

        weka.core.EuclideanDistance referenceImplementation = this.getReferenceDerivateDistance(alpha, true);
        ITimeSeriesDistance ownImplementation = new DerivateDistance(alpha, new BackwardDifferenceDerivate(),
                new DynamicTimeWarping(ScalarDistanceUtil.getSquaredDistance()));

        // String result =
        // DistanceRefTestUtil.runCorrectnessTest(referenceImplementation,
        // ownImplementation, arffFile,
        // delta);

        // assertNull(result, result);
        // The calculate the square root on both, the value and derivate distance,
        // before combining them (see line 535). This behaviour is not correct.
    }

    @Test
    public void testPerformanceForDistanceCalculationUsingEuclideanDistanceOnCarDataset()
            throws IOException, TimeSeriesLoadingException {
        double alpha = 0.5;
        double delta = 0.001;
        File arffFile = DistanceRefTestUtil.getCarArffFile();

        weka.core.EuclideanDistance referenceImplementation = this.getReferenceDerivateDistance(alpha, false);
        ITimeSeriesDistance ownImplementation = new DerivateDistance(alpha, new BackwardDifferenceDerivate(),
                new EuclideanDistance());

        Pair<Double, Double> times = DistanceRefTestUtil.runPerformanceTest(referenceImplementation, ownImplementation,
                arffFile);
        double refTime = times.getX();
        double ownTime = times.getY();
        String message = String.format("Reference implementation was faster. Reference: %.3f ms, own: %.3f ms", refTime,
                ownTime);

        System.out.println(String.format("Reference: %.3f ms, own: %.3f ms", refTime, ownTime));
        assertTrue(message, refTime > ownTime);
    }

    @Test
    public void testPerformanceForDistanceCalculationUsingDTWOnCarDataset()
            throws IOException, TimeSeriesLoadingException {
        double alpha = 0.5;
        File arffFile = DistanceRefTestUtil.getCarArffFile();

        weka.core.EuclideanDistance referenceImplementation = this.getReferenceDerivateDistance(alpha, true);
        ITimeSeriesDistance ownImplementation = new DerivateDistance(alpha, new BackwardDifferenceDerivate(),
                new DynamicTimeWarping(ScalarDistanceUtil.getSquaredDistance()));

        Pair<Double, Double> times = DistanceRefTestUtil.runPerformanceTest(referenceImplementation, ownImplementation,
                arffFile);
        double refTime = times.getX();
        double ownTime = times.getY();
        String message = String.format("Reference implementation was faster. Reference: %.3f ms, own: %.3f ms", refTime,
                ownTime);

        System.out.println(String.format("Reference: %.3f ms, own: %.3f ms", refTime, ownTime));
        assertTrue(message, refTime > ownTime);
    }

    public weka.core.EuclideanDistance getReferenceDerivateDistance(double alpha, boolean dtw) {
        // https://bitbucket.org/TonyBagnall/time-series-classification/src/f4fe66b74e039b0475a87ebf6d57db400da25b63/TimeSeriesClassification/src/tsc_algorithms/DD_DTW.java?at=default&fileviewer=file-view-default
        return dtw ? new GoreckiDerivativesDTW(alpha) : new GoreckiDerivativesEuclideanDistance(alpha);

    }

    public kNN getReferenceDerivateDistanceWithNearestNeighbor() {
        DD_DTW nnDD_ED = new DD_DTW(DistanceType.EUCLIDEAN); // Uses Euclidean Distance.
        DD_DTW nnDD_DTW = new DD_DTW(DistanceType.DTW); // Uses DTW.
        return nnDD_ED;
    }

}
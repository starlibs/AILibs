package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import timeseriesweka.elastic_distance_measures.MSMDistance;
import weka.classifiers.lazy.kNN;

public class MoveSplitMergeRefTest {

    /**
     * Checks correctness for distance calulcation on a whole dataset by comparing
     * calulated distances.
     */
    @Test
    public void testCorrectnessForDistanceCalculationUsingEuclideanDistanceOnCarDataset()
            throws IOException, TimeSeriesLoadingException {
        double c = 1;

        double delta = 0.001;
        File arffFile = DistanceRefTestUtil.getCarArffFile();

        weka.core.EuclideanDistance referenceImplementation = this.getReferenceMoveSplitMerge(c);
        ITimeSeriesDistance ownImplementation = new MoveSplitMerge(c);
        String result = DistanceRefTestUtil.runCorrectnessTest(referenceImplementation, ownImplementation, arffFile,
                delta);

        assertNull(result, result);
    }

    /**
     * Compares the performance for the distance calculation on a whole dataset by
     * measuring calculation time.
     */
    @Test
    public void testPerformanceForDistanceCalculationOnCarDataset() throws IOException, TimeSeriesLoadingException {
        double c = 1;
        File arffFile = DistanceRefTestUtil.getCarArffFile();

        weka.core.EuclideanDistance referenceImplementation = this.getReferenceMoveSplitMerge(c);
        ITimeSeriesDistance ownImplementation = new MoveSplitMerge(c);

        Pair<Double, Double> times = DistanceRefTestUtil.runPerformanceTest(referenceImplementation, ownImplementation,
                arffFile);
        double refTime = times.getX();
        double ownTime = times.getY();
        String message = String.format("Reference implementation was faster. Reference: %.3f ms, own: %.3f ms", refTime,
                ownTime);

        System.out.println(String.format("Reference: %.3f ms, own: %.3f ms", refTime, ownTime));
        assertTrue(message, refTime > ownTime);
    }

    public weka.core.EuclideanDistance getReferenceMoveSplitMerge(double c) {
        MSMDistance referenceMoveSplitMerge = new MSMDistance(c);
        return referenceMoveSplitMerge;
    }

    public kNN getReferenceNearestNeighborWithMoveSplitMerge(double c) {
        kNN knn = new kNN();
        weka.core.EuclideanDistance distance = getReferenceMoveSplitMerge(c);
        knn.setDistanceFunction(distance);
        return knn;
    }
}
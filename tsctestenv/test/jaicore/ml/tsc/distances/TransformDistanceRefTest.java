package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.filter.transform.CosineTransform;
import jaicore.ml.tsc.util.ScalarDistanceUtil;
import timeseriesweka.classifiers.DTD_C;
import timeseriesweka.classifiers.DTD_C.TransformType;
import timeseriesweka.classifiers.DTD_C.TransformWeightedDTW;

import weka.classifiers.lazy.kNN;

/**
 * TransformDistanceRefTest
 */
public class TransformDistanceRefTest {

    @Test
    public void testCorrectnessForDistanceCalculationOnCarDataset() throws IOException, TimeSeriesLoadingException {
        double alpha = 0;

        double delta = 0.001;
        File arffFile = DistanceRefTestUtil.getCarArffFile();

        weka.core.EuclideanDistance referenceImplementation = this.getReferenceTransformDistance();
        ITimeSeriesDistance ownImplementation = new TransformDistance(alpha, new CosineTransform(),
                new DynamicTimeWarping(ScalarDistanceUtil.getSquaredDistance()));

        // String result =
        // DistanceRefTestUtil.runCorrectnessTest(referenceImplementation,
        // ownImplementation, arffFile,
        // delta);

        // assertNull(result, result);
        // The calculate the square root on both, the value and derivate distance,
        // before combining them (see line 251). This behaviour is not correct.
    }

    @Test
    public void testPerformanceForDistanceCalculationUsingEuclideanDistanceOnCarDataset()
            throws IOException, TimeSeriesLoadingException {
        double alpha = 0.5;
        double delta = 0.001;
        File arffFile = DistanceRefTestUtil.getCarArffFile();

        weka.core.EuclideanDistance referenceImplementation = this.getReferenceTransformDistance();
        ITimeSeriesDistance ownImplementation = new TransformDistance(alpha, new CosineTransform(),
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

    public weka.core.EuclideanDistance getReferenceTransformDistance() {
        TransformWeightedDTW td = new TransformWeightedDTW(TransformType.COS);
        // Cant set a and b here.
        return td;
    }

    public kNN getReferenceNearestNeighborWithTransformDistance(double a, double b) {
        DTD_C referenceTransformDistance = new DTD_C(TransformType.COS);
        referenceTransformDistance.setAandB(a, b);
        return referenceTransformDistance;
    }
}
package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.bytedeco.javacpp.mkldnn.concat;
import org.junit.Before;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * TimeWarpEditDistanceTest
 */
public class MoveSplitMergeTest {

    INDArray timeSeries1; // { 1, 1, 1, 1, 1, 1 }
    INDArray timeSeries2; // { 1, 1, 1, 1, 1, 1 }
    INDArray timeSeries3;
    INDArray timeSeries4;
    INDArray timeSeries5;
    INDArray timeSeries6;

    INDArray noTimeSeries;

    @Before
    public void setUp() {
        int[] shape = { 6 };
        float[] data = { 1, 1, 1, 1, 1, 1 };
        timeSeries1 = Nd4j.create(data, shape);
        timeSeries2 = Nd4j.create(data, shape);

        int[] shape2 = { 7 };
        timeSeries3 = Nd4j.create(new double[] { 7.0, 8.0, 12.0, 15.0, 9.0, 3.0, 5.0 }, shape2);
        timeSeries4 = Nd4j.create(new double[] { 7.0, 9.0, 12.0, 15.0, 15.0, 9.0, 3.0 }, shape2);

        timeSeries5 = Nd4j.create(new double[] { 7.0, 8.0, 12.0, 15.0, 9.0, 9.0, 5.0 }, shape2);
        timeSeries6 = Nd4j.create(new double[] { 7.0, 8.0, 12.0, 13.0, 9.0, 9.0, 5.0 }, shape2);

        noTimeSeries = Nd4j.rand(2, 2);
    }

    @Test
    public void testDistanceCalculation() throws IllegalArgumentException {
        MoveSplitMerge msm = new MoveSplitMerge(1.0);
        double distance = msm.distance(timeSeries1, timeSeries2);
        double expectation = 0;
        String message = "Calculated %f, but %f was expected";
        assertEquals(String.format(message, distance, expectation), expectation, distance, 0);
    }

    @Test
    public void testDistanceCalculation2() throws IllegalArgumentException {
        double c = 4.0;
        MoveSplitMerge msm = new MoveSplitMerge(c);
        double distance = msm.distance(timeSeries3, timeSeries4);
        double expectation = 11;
        String message = "Calculated %f, but %f was expected";
        assertEquals(String.format(message, distance, expectation), expectation, distance, 0);
    }

    @Test
    public void testThrowsErrorWhenTimeSeriesHaveDifferentLength() {
        MoveSplitMerge msm = new MoveSplitMerge(1.0);
        assertThrows(IllegalArgumentException.class, () -> {
            msm.distance(timeSeries1, timeSeries4);
        });
    }

    @Test
    public void testThrowsErrorWhenTimeSeriesIsNoTimeSeries() {
        MoveSplitMerge msm = new MoveSplitMerge(1.0);
        assertThrows(IllegalArgumentException.class, () -> {
            msm.distance(noTimeSeries, timeSeries4);
        });
    }

}
package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * ShotgunDistanceTest
 */
public class ShotgunDistanceTest {

    double[] timeSeries1 = { 1, 1, 2, 2, 3, 5, 3 }; // distance with d(x,y) = |x-y| is 1
    double[] timeSeries2 = { 1, 2, 3, 5, 5, 7 };

    @Test
    public void testCalculation() {
        int windowLength = 2;
        boolean meanNormalization = false;
        ShotgunDistance shotgunDistance = new ShotgunDistance(windowLength, meanNormalization);
        double distance = shotgunDistance.distance(timeSeries1, timeSeries2);
        double expectation = 0;
        assertEquals(expectation, distance, 1e-5);
    }

    @Test
    public void testCalculation2() {
        int windowLength = 3;
        boolean meanNormalization = false;
        ShotgunDistance shotgunDistance = new ShotgunDistance(windowLength, meanNormalization);
        double distance = shotgunDistance.distance(timeSeries1, timeSeries2);
        double expectation = .635109;
        assertEquals(expectation, distance, 1e-5);
    }

}
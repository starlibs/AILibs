package jaicore.ml.tsc.distances;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.util.ClassMapper;
import jaicore.ml.tsc.util.SimplifiedTimeSeriesLoader;

/**
 * DynamicTimeWarpingRefTest
 */
public class DynamicTimeWarpingRefTest {

    private static final String CAR_TRAIN = "/home/rtf/Data/tsc/Car/Car_TRAIN.arff";
    private static final String CAR_TEST = "/home/rtf/Data/tsc/Car/Car_TEST.arff";

    @Before
    public void setUp() {
        File file = new File(CAR_TRAIN);
        Pair<TimeSeriesDataset, ClassMapper> trainPair;

        try {
            trainPair = SimplifiedTimeSeriesLoader.loadArff(file);
            TimeSeriesDataset train = trainPair.getX();
        } catch (TimeSeriesLoadingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void compareDistanceCalculations() {
        System.out.println("HEllo");
        int x = 2 * 3;
        assertEquals(x, 7);
    }

}
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
import jaicore.ml.tsc.util.TimeSeriesUtil;
import timeseriesweka.elastic_distance_measures.TWEDistance;

/**
 * TimeWarpEditDistanceRefTest
 */
public class TimeWarpEditDistanceRefTest {

    /** Local path for the datasets arff files. */
    private static final String PATH = "./tsctestenv/data/univariate/";

    /** Path for pen digits dataset. */
    private static final String DATASET_PATH = PATH + "ItalyPowerDemand/ItalyPowerDemand_TRAIN.arff";

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
        File arffFile = new File(DATASET_PATH);
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
        double nu = 1;
        double lambda = 1;

        TWEDistance referenceTimeWarpEditDsitance = new TWEDistance(lambda, nu);
        TimeWarpEditDistance timeWarpEditDistance = new TimeWarpEditDistance(lambda, nu);

        double[][] values = dataset.getValues(0);
        for (int i = 0; i < 100; i++) {
            for (int j = i; j < values.length; j++) {
                double referenceDistance = referenceTimeWarpEditDsitance.distance(values[i], values[j], cutoff);
                double distance = timeWarpEditDistance.distance(values[i], values[j]);
                String message = String.format("Distance between %s and %s.", TimeSeriesUtil.toString(values[i]),
                        TimeSeriesUtil.toString(values[j]));
                assertEquals(message, referenceDistance, distance, 10e-5);
                System.out.println("Correct");
            }
        }
    }

    @Test
    public void testReferenceCorrectness() {
        double[] timeSeries5 = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        double[] timeSeries6 = { 1, 2, 3, 4, 5, 6, 7, 8, 12 };
        double[] timeSeries7 = { 0, 3, 2, 5, 4, 7, 6, 9, 8 };

        double nu = 0.001;
        double lambda = 1;

        TWEDistance referenceTimeWarpEditDsitance = new TWEDistance(lambda, nu);
        double distance = referenceTimeWarpEditDsitance.distance(timeSeries5, timeSeries6, Double.MAX_VALUE);
        assertEquals(3, distance, 0);

        double distance2 = referenceTimeWarpEditDsitance.distance(timeSeries5, timeSeries7, Double.MAX_VALUE);
        assertEquals(17, distance2, 0);

    }
}
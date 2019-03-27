package jaicore.ml.tsc.distances;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.tsc.util.ClassMapper;
import jaicore.ml.tsc.util.SimplifiedTimeSeriesLoader;
import jaicore.ml.tsc.util.TimeSeriesUtil;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.util.ScalarDistanceUtil;

import timeseriesweka.classifiers.NN_CID.CIDDistance;
import timeseriesweka.elastic_distance_measures.BasicDTW;
import timeseriesweka.elastic_distance_measures.DTW;
import timeseriesweka.elastic_distance_measures.MSMDistance;
import timeseriesweka.elastic_distance_measures.TWEDistance;
import timeseriesweka.elastic_distance_measures.WeightedDTW;
import weka.classifiers.lazy.kNN;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

import timeseriesweka.classifiers.DD_DTW.GoreckiDerivativesEuclideanDistance;
import timeseriesweka.classifiers.DTD_C.TransformType;
import timeseriesweka.classifiers.DTD_C.TransformWeightedDTW;

import timeseriesweka.classifiers.DD_DTW;
import timeseriesweka.classifiers.DTD_C;
import timeseriesweka.classifiers.NN_CID;
import timeseriesweka.classifiers.DD_DTW.DistanceType;
import timeseriesweka.classifiers.DD_DTW.GoreckiDerivativesDTW;

public class DistanceRefTestUtil {

    /** Local path for the datasets arff files. */
    private static final String PATH = "./tsctestenv/data/univariate/";

    /** Path for car dataset. */
    private static final String CAR = PATH + "PenDigits/PenDigitsDimension1_TRAIN.arff";

    public static String runCorrectnessTest(weka.core.EuclideanDistance referenceImplementation,
            ITimeSeriesDistance ownImplementation, File arffFile, double delta)
            throws IOException, TimeSeriesLoadingException {
        // Set cutoff to max for now.
        double cutoff = Double.MAX_VALUE;

        // Load weka instances and time series dataset.
        Instances wekaInstances = DistanceRefTestUtil.loadWekaInstances(arffFile);

        TimeSeriesDataset dataset = DistanceRefTestUtil.loadDataset(arffFile);
        double[][] values = dataset.getValues(0);

        int numberOfInstances = values.length;

        // Compare distances calculation for all possible pairs of time series.
        for (int i = 0; i < numberOfInstances; i++) {
            for (int j = i; j < numberOfInstances; j++) {
                // Values to compare distance calculation for.
                Instance weka1 = wekaInstances.get(i);
                Instance weka2 = wekaInstances.get(j);
                double[] value1 = values[i];
                double[] value2 = values[j];

                // Calculate with reference implementation.
                double referenceDistance = referenceImplementation.distance(weka1, weka2, cutoff);
                // Calculate with own implementation.
                double ownDistance = ownImplementation.distance(value1, value2);

                // Return message when difference in distance calculation is greather than
                // delta.
                if (Math.abs(referenceDistance - ownDistance) > delta) {
                    String message = String.format("Distance between %s and %s. Own %f, reference: %f",
                            TimeSeriesUtil.toString(value1), TimeSeriesUtil.toString(value2), ownDistance,
                            referenceDistance);
                    return message;
                }
            }
        }

        // Return null, if all distances are calculated equal.
        return null;
    }

    public static Pair<Double, Double> runPerformanceTest(weka.core.EuclideanDistance referenceImplementation,
            ITimeSeriesDistance ownImplementation, File arffFile) throws IOException, TimeSeriesLoadingException {
        // Set cutoff to max for now.
        double cutoff = Double.MAX_VALUE;

        // Load weka instances and time series dataset.
        Instances wekaInstances = DistanceRefTestUtil.loadWekaInstances(arffFile);

        TimeSeriesDataset dataset = DistanceRefTestUtil.loadDataset(arffFile);
        double[][] values = dataset.getValues(0);

        int numberOfInstances = values.length;

        // Measure time for own implementation.
        double ownStart = System.currentTimeMillis();
        for (int i = 0; i < numberOfInstances; i++) {
            for (int j = i; j < numberOfInstances; j++) {
                ownImplementation.distance(values[i], values[j]);
            }
        }
        double ownEnd = System.currentTimeMillis();

        // Measure time for reference implementation.
        double refStart = System.currentTimeMillis();
        for (int i = 0; i < numberOfInstances; i++) {
            for (int j = i; j < numberOfInstances; j++) {
                referenceImplementation.distance(wekaInstances.get(i), wekaInstances.get(j), cutoff);
            }
        }
        double refEnd = System.currentTimeMillis();

        // Calculate time difference.
        double refTime = refEnd - refStart;
        double ownTime = ownEnd - ownStart;

        return new Pair<Double, Double>(refTime, ownTime);

    }

    public static Instances loadWekaInstances(File arffFile) throws IOException {
        ArffReader arffReader = new ArffReader(new FileReader(arffFile));
        final Instances wekaInstances = arffReader.getData();
        wekaInstances.setClassIndex(wekaInstances.numAttributes() - 1);
        return wekaInstances;
    }

    public static TimeSeriesDataset loadDataset(File arffFile) throws TimeSeriesLoadingException {
        Pair<TimeSeriesDataset, ClassMapper> trainPair = SimplifiedTimeSeriesLoader.loadArff(arffFile);
        return trainPair.getX();
    }

    public static File getCarArffFile() {
        return new File(CAR);
    }

}
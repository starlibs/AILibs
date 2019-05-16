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

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class DistanceRefTestUtil {

    /** Local path for the datasets arff files. */
    private static final String PATH = "./tsctestenv/data/univariate/";

    /** Path for car dataset. */
    private static final String CAR = PATH + "Car/Car_TRAIN.arff";

    private static final String ARROW_HEAD = PATH + "crickitX/crickitX_TRAIN.arff";

    private static final String BEEF = PATH + "Beef/Beef_TEST.arff";

    private static final String ELECTRIC_DEVICES = PATH + "computers/computers_TRAIN.arff";

    private static final String CRICKIT_X = PATH + "CricketX/CricketX_TRAIN.arff"; // 390 x 300

    private static final String COMPUTERS = PATH + "Computers/Computers_TRAIN.arff"; // 250 x 720

    private static final String ECG200 = PATH + "ECG200/ECG200_TRAIN.arff"; // 100 x 96

    private static final String ITALY_POWER_DEMAND = PATH + "ItalyPowerDemand/ItalyPowerDemand_TRAIN.arff"; // 67 x 24

    private static final String SYNTHETIC_CONTROL = PATH + "SyntheticControl/SyntheticControl_TRAIN.arff"; // 300 x 60

    private static final String CHINATOWN = PATH + "Chinatown/Chinatown_TRAIN.arff";

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

    public static void evaluatePerformance(String descr, weka.core.EuclideanDistance referenceImplementation,
            ITimeSeriesDistance ownImplementation) throws IOException, TimeSeriesLoadingException {
        int runs = 4;
        double[] tChinatown_own = new double[runs];
        double[] tChinatown_ref = new double[runs];
        double[] tEcg200_own = new double[runs];
        double[] tEcg200_ref = new double[runs];
        double[] tSYNTHETIC_CONTROL_own = new double[runs];
        double[] tSYNTHETIC_CONTROL_ref = new double[runs];

        File crickitX = new File(CHINATOWN);
        File ecg200 = new File(ECG200);
        File synthetic = new File(SYNTHETIC_CONTROL);

        for (int i = 0; i < runs; i++) {
            // crickitX
            Pair<Double, Double> crickitXTimes = DistanceRefTestUtil.runPerformanceTest(referenceImplementation,
                    ownImplementation, crickitX);
            tChinatown_ref[i] = crickitXTimes.getX();
            tChinatown_own[i] = crickitXTimes.getY();
        }
        System.out.println(String.format("%s on chinatown dataset - Own: %.3f ms (%.3f), Ref: %.3f ms (%.3f), p = %.3f",
                descr, TimeSeriesUtil.mean(tChinatown_own), TimeSeriesUtil.standardDeviation(tChinatown_own),
                TimeSeriesUtil.mean(tChinatown_ref), TimeSeriesUtil.standardDeviation(tChinatown_ref),
                TimeSeriesUtil.mean(tChinatown_own) / TimeSeriesUtil.mean(tChinatown_ref)));

        // for (int i = 0; i < runs; i++) {
        // // ecg200
        // Pair<Double, Double> ecg200Times =
        // DistanceRefTestUtil.runPerformanceTest(referenceImplementation,
        // ownImplementation, ecg200);
        // tEcg200_ref[i] = ecg200Times.getX();
        // tEcg200_own[i] = ecg200Times.getY();
        // }
        // System.out.println(String.format("%s on ecg200 dataset - Own: %.3f ms (%.3f),
        // Ref: %.3f ms (%.3f). p = %.3f",
        // descr, TimeSeriesUtil.mean(tEcg200_own),
        // TimeSeriesUtil.standardDeviation(tEcg200_own),
        // TimeSeriesUtil.mean(tEcg200_ref),
        // TimeSeriesUtil.standardDeviation(tEcg200_ref),
        // TimeSeriesUtil.mean(tEcg200_own) / TimeSeriesUtil.mean(tEcg200_ref)));

        for (int i = 0; i < runs; i++) {
            // computers
            Pair<Double, Double> syntheticTimes = DistanceRefTestUtil.runPerformanceTest(referenceImplementation,
                    ownImplementation, synthetic);
            tSYNTHETIC_CONTROL_ref[i] = syntheticTimes.getX();
            tSYNTHETIC_CONTROL_own[i] = syntheticTimes.getY();
        }
        System.out.println(String.format(
                "%s on SYNTHETIC_CONTROL dataset - Own: %.3f ms (%.3f), Ref: %.3f ms (%.3f). p = %.3f", descr,
                TimeSeriesUtil.mean(tSYNTHETIC_CONTROL_own), TimeSeriesUtil.standardDeviation(tSYNTHETIC_CONTROL_own),
                TimeSeriesUtil.mean(tSYNTHETIC_CONTROL_ref), TimeSeriesUtil.standardDeviation(tSYNTHETIC_CONTROL_ref),
                TimeSeriesUtil.mean(tSYNTHETIC_CONTROL_own) / TimeSeriesUtil.mean(tSYNTHETIC_CONTROL_ref)));
        System.out.println("-----");

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
            for (int j = 0; j < numberOfInstances; j++) {
                ownImplementation.distance(values[i], values[j]);
            }
        }
        double ownEnd = System.currentTimeMillis();

        // Measure time for reference implementation.
        double refStart = System.currentTimeMillis();
        for (int i = 0; i < numberOfInstances; i++) {
            for (int j = 0; j < numberOfInstances; j++) {
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
package jaicore.ml.tsc.classifier.neighbors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import jaicore.ml.core.exception.EvaluationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.util.EvaluationUtil;
import jaicore.ml.tsc.util.SimplifiedTimeSeriesLoader;
import sfa.classification.Classifier.Predictions;
import sfa.timeseries.TimeSeries;

/**
 * ShotgunEnsembleClassifierRefTest
 */
public class ShotgunEnsembleClassifierRefTest {

    private static final String PATH = "./tsctestenv/data/univariate/";

    private static final String CAR_TRAIN = PATH + "Car/Car_TRAIN.arff";
    private static final String CAR_TEST = PATH + "Car/Car_TEST.arff";

    @Test
    public void testClassifier() throws FileNotFoundException, EvaluationException, TrainingException,
            PredictionException, IOException, TimeSeriesLoadingException, ClassNotFoundException {

        int minWindowLength = 5; // This is hard coded in reference implementation.
        int maxWindowLength = 10;
        boolean meanNormalization = true;
        double factor = 0.92; // Default in reference implementation.

        ShotgunEnsembleAlgorithm algorithm = new ShotgunEnsembleAlgorithm(minWindowLength, maxWindowLength,
                meanNormalization);
        ShotgunEnsembleClassifier ownClf = new ShotgunEnsembleClassifier(algorithm, factor);

        sfa.classification.ShotgunClassifier.MAX_WINDOW_LENGTH = maxWindowLength;
        sfa.classification.Classifier.NORMALIZATION = new boolean[] { meanNormalization };
        sfa.classification.ShotgunEnsembleClassifier refClf = new sfa.classification.ShotgunEnsembleClassifier();

        // Load data for classifiers.
        TimeSeriesDataset trainOwn = SimplifiedTimeSeriesLoader.loadArff(new File(CAR_TRAIN)).getX();
        TimeSeriesDataset testOwn = SimplifiedTimeSeriesLoader.loadArff(new File(CAR_TEST)).getX();

        TimeSeries[] trainRef = loadSFATimeSeriesFromFile(new File(CAR_TRAIN));
        TimeSeries[] testRef = loadSFATimeSeriesFromFile(new File(CAR_TEST));

        // Train own.
        long ownTrainingStart = System.currentTimeMillis();
        ownClf.train(trainOwn);
        long ownTrainingEnd = System.currentTimeMillis();
        // Test own.
        long ownTestStart = System.currentTimeMillis();
        List<Integer> ownPredictions = ownClf.predict(testOwn);
        long ownTestEnd = System.currentTimeMillis();
        double ownAccuracy = EvaluationUtil.accuracy(ownPredictions, testOwn.getTargets());

        // Train ref.
        long refTrainingStart = System.currentTimeMillis();
        refClf.fit(trainRef);
        long refTrainingEnd = System.currentTimeMillis();
        // Test ref.
        long refTestStart = System.currentTimeMillis();
        Predictions refPredictions = refClf.score(testRef);
        long refTestEnd = System.currentTimeMillis();
        double refAccuracy = refPredictions.correct.get() / refPredictions.labels.length;

        // Print results.
        System.out.println(String.format("Own: Train time: %d, Test time: %d, Accuracy: %f",
                (ownTrainingEnd - ownTrainingStart), (ownTestEnd - ownTestStart), ownAccuracy));
        System.out.println(String.format("Ref: Train time: %d, Test time: %d, Accuracy: %f",
                (refTrainingEnd - refTrainingStart), (refTestEnd - refTestStart), refAccuracy));
    }

    public TimeSeries[] loadSFATimeSeriesFromFile(File arffFile) throws TimeSeriesLoadingException {
        TimeSeriesDataset dataset = SimplifiedTimeSeriesLoader.loadArff(arffFile).getX();
        double[][] values = dataset.getValuesOrNull(0);
        int[] targets = dataset.getTargets();

        TimeSeries[] timeSeries = new TimeSeries[dataset.getNumberOfInstances()];
        for (int i = 0; i < dataset.getNumberOfInstances(); i++) {
            timeSeries[i] = new TimeSeries(values[i], (double) targets[i]);
        }
        return timeSeries;
    }
}
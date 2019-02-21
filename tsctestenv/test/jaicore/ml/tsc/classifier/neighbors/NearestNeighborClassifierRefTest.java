package jaicore.ml.tsc.classifier.neighbors;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import jaicore.ml.core.exception.EvaluationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.classifier.neighbors.NearestNeighborClassifier;
import jaicore.ml.tsc.classifier.SimplifiedTSClassifierTest;
import jaicore.ml.tsc.distances.DynamicTimeWarping;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.util.ScalarDistanceUtil;
import timeseriesweka.elastic_distance_measures.BasicDTW;

import weka.classifiers.lazy.kNN;

/**
 * NearestNeighborClassifierRefTest
 */
public class NearestNeighborClassifierRefTest {

    private static final String PATH = "./tsctestenv/data/univariate/";

    private static final String CAR_TRAIN = PATH + "Car/Car/Car_TRAIN.arff";
    private static final String CAR_TEST = PATH + "Car/Car/Car_TEST.arff";

    @Test
    public void testClassifier() throws FileNotFoundException, EvaluationException, TrainingException,
            PredictionException, IOException, TimeSeriesLoadingException, ClassNotFoundException {

        final int k = 1;

        kNN refClf = new kNN(); // k = 1 by default
        BasicDTW refTimeWarpEditDistance = new BasicDTW(); // public TWEDistance(double nu=1, double lambda=1)
        refClf.setDistanceFunction(refTimeWarpEditDistance);

        NearestNeighborClassifier ownClf = new NearestNeighborClassifier(k,
                new DynamicTimeWarping(ScalarDistanceUtil.getSquaredDistance()));

        Map<String, Object> result = SimplifiedTSClassifierTest.compareClassifiers(refClf, ownClf, 0, null, null,
                new File(CAR_TRAIN), new File(CAR_TEST));

        System.out.println(result.toString());
    }

}
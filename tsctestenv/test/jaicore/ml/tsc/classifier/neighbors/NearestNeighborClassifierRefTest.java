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

        /** Path for car dataset. */
        private static final String CAR = PATH + "Car/Car_TRAIN.arff";

        private static final String ARROW_HEAD = PATH + "crickitX/crickitX_TRAIN.arff";

        private static final String BEEF = PATH + "Beef/Beef_TEST.arff";

        private static final String ELECTRIC_DEVICES = PATH + "computers/computers_TRAIN.arff";

        private static final String CRICKIT_X = PATH + "CricketX/CricketX_TRAIN.arff"; // 390 x 300
        private static final String CRICKIT_TEST = PATH + "CricketX/CricketX_TEST.arff";

        private static final String COMPUTERS = PATH + "Computers/Computers_TRAIN.arff"; // 250 x 720

        private static final String ECG200 = PATH + "ECG200/ECG200_TRAIN.arff"; // 100 x 96
        private static final String ECG200_TEST = PATH + "ECG200/ECG200_TEST.arff";

        private static final String ITALY_POWER_DEMAND = PATH + "ItalyPowerDemand/ItalyPowerDemand_TRAIN.arff"; // 67 x
                                                                                                                // 24

        private static final String SYNTHETIC_CONTROL = PATH + "SyntheticControl/SyntheticControl_TRAIN.arff"; // 300 x
        private static final String SYNTHETIC_CONTROL_TEST = PATH + "SyntheticControl/SyntheticControl_TEST.arff"; // 300
                                                                                                                   // x
                                                                                                                   // 60

        private static final String CHINATOWN = PATH + "Chinatown/Chinatown_TRAIN.arff";

        @Test
        public void testClassifier() throws FileNotFoundException, EvaluationException, TrainingException,
                        PredictionException, IOException, TimeSeriesLoadingException, ClassNotFoundException {
                final int k = 1;

                kNN refClf = new kNN(); // k = 1 by default
                BasicDTW refTimeWarpEditDistance = new BasicDTW(); // public TWEDistance(double nu=1, double lambda=1)
                refClf.setDistanceFunction(refTimeWarpEditDistance);

                NearestNeighborClassifier ownClf = new NearestNeighborClassifier(k,
                                new DynamicTimeWarping(ScalarDistanceUtil.getSquaredDistance()));

                Map<String, Object> result = SimplifiedTSClassifierTest.compareClassifiers(refClf, ownClf, 0, null,
                                null, new File(CRICKIT_X), new File(CRICKIT_TEST));

                System.out.println(result.toString());
        }

        @Test
        public void testClassifier2() throws FileNotFoundException, EvaluationException, TrainingException,
                        PredictionException, IOException, TimeSeriesLoadingException, ClassNotFoundException {
                final int k = 1;

                kNN refClf = new kNN(); // k = 1 by default
                BasicDTW refTimeWarpEditDistance = new BasicDTW(); // public TWEDistance(double nu=1, double lambda=1)
                refClf.setDistanceFunction(refTimeWarpEditDistance);

                NearestNeighborClassifier ownClf = new NearestNeighborClassifier(k,
                                new DynamicTimeWarping(ScalarDistanceUtil.getSquaredDistance()));

                Map<String, Object> result = SimplifiedTSClassifierTest.compareClassifiers(refClf, ownClf, 0, null,
                                null, new File(ECG200), new File(ECG200_TEST));

                System.out.println(result.toString());
        }

        @Test
        public void testClassifier3() throws FileNotFoundException, EvaluationException, TrainingException,
                        PredictionException, IOException, TimeSeriesLoadingException, ClassNotFoundException {
                final int k = 1;

                kNN refClf = new kNN(); // k = 1 by default
                BasicDTW refTimeWarpEditDistance = new BasicDTW(); // public TWEDistance(double nu=1, double lambda=1)
                refClf.setDistanceFunction(refTimeWarpEditDistance);

                NearestNeighborClassifier ownClf = new NearestNeighborClassifier(k,
                                new DynamicTimeWarping(ScalarDistanceUtil.getSquaredDistance()));

                Map<String, Object> result = SimplifiedTSClassifierTest.compareClassifiers(refClf, ownClf, 0, null,
                                null, new File(SYNTHETIC_CONTROL), new File(SYNTHETIC_CONTROL_TEST));

                System.out.println(result.toString());
        }

}
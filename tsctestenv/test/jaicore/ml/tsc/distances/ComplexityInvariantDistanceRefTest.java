package jaicore.ml.tsc.distances;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNull;
import org.junit.Test;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.tsc.complexity.SquaredBackwardDifferenceComplexity;
import jaicore.ml.tsc.complexity.StretchingComplexity;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.util.ScalarDistanceUtil;
import timeseriesweka.classifiers.NN_CID.CIDDistance;
import timeseriesweka.classifiers.NN_CID.CIDDTWDistance;

/**
 * ComplexityInvariantDistanceRefTest
 */
public class ComplexityInvariantDistanceRefTest {

        /**
         * Checks correctness for distance calulcation on a whole dataset by comparing
         * calulated distances.
         */
        @Test
        public void testCorrectnessForDistanceCalculationUsingEuclideanDistanceOnCarDataset()
                        throws IOException, TimeSeriesLoadingException {
                double delta = 0.001;
                File arffFile = DistanceRefTestUtil.getCarArffFile();

                weka.core.EuclideanDistance referenceImplementation = new CIDDistance();
                ITimeSeriesDistance ownImplementation = new ComplexityInvariantDistance(new EuclideanDistance(),
                                new SquaredBackwardDifferenceComplexity());
                String result = DistanceRefTestUtil.runCorrectnessTest(referenceImplementation, ownImplementation,
                                arffFile, delta);

                assertNull(result, result);

                // In their code, their square root twice for the Euclidean Distance (in line
                // 199 and 211). They also add some delta (in line 20 and 204) to the complexity
                // estimates. Therefore, their code is assumed to be incorrect. No comparison
                // made here.
        }

        /**
         * Checks correctness for distance calulcation on a whole dataset by comparing
         * calulated distances.
         */
        @Test
        public void testCorrectnessForDistanceCalculationUsingDTWOnCarDataset()
                        throws IOException, TimeSeriesLoadingException {
                double delta = 0.001;
                File arffFile = DistanceRefTestUtil.getCarArffFile();

                weka.core.EuclideanDistance referenceImplementation = new CIDDTWDistance();
                ITimeSeriesDistance ownImplementation = new ComplexityInvariantDistance(
                                new DynamicTimeWarping(ScalarDistanceUtil.getAbsoluteDistance()),
                                new SquaredBackwardDifferenceComplexity());
                String result = DistanceRefTestUtil.runCorrectnessTest(referenceImplementation, ownImplementation,
                                arffFile, delta);

                assertNull(result, result);

                // Their code is wrong again.
        }

        /**
         * Compares the performance for the distance calculation on a whole dataset by
         * measuring calculation time.
         */
        @Test
        public void testPerformanceForDistanceCalculationUsingEuclideanDistanceOnCarDataset()
                        throws IOException, TimeSeriesLoadingException {
                File arffFile = DistanceRefTestUtil.getCarArffFile();
                testPerformanceUsingEuclideanDistance(arffFile, "car");
        }

        /**
         * Compares the performance for the distance calculation on a whole dataset by
         * measuring calculation time.
         */
        @Test
        public void testPerformanceForDistanceCalculationUsingDTWOnCarDataset()
                        throws IOException, TimeSeriesLoadingException {
                File arffFile = DistanceRefTestUtil.getCarArffFile();
                testPerformanceUsingDTW(arffFile, "car");
        }

        /**
         * Measure calculation time for own and reference implementation with Euclidean
         * Distance.
         * 
         * @param arffFile Dataset to measure time on.
         * @param fileName Used in string output.
         * @throws IOException
         * @throws TimeSeriesLoadingException
         */
        public void testPerformanceUsingEuclideanDistance(File arffFile, String fileName)
                        throws IOException, TimeSeriesLoadingException {
                weka.core.EuclideanDistance referenceImplementation = new CIDDistance();
                ITimeSeriesDistance ownImplementation = new ComplexityInvariantDistance(new EuclideanDistance(),
                                new StretchingComplexity());
                Pair<Double, Double> times = DistanceRefTestUtil.runPerformanceTest(referenceImplementation,
                                ownImplementation, arffFile);
                double refTime = times.getX();
                double ownTime = times.getY();

                System.out.println(String.format("CID. %s dataset - Reference: %.3f ms, own: %.3f ms", fileName,
                                refTime, ownTime));
        }

        /**
         * Measure calculation time for own and reference implementation with DTW.
         * 
         * @param arffFile Dataset to measure time on.
         * @param fileName Used in string output.
         * @throws IOException
         * @throws TimeSeriesLoadingException
         */
        public void testPerformanceUsingDTW(File arffFile, String fileName)
                        throws IOException, TimeSeriesLoadingException {
                weka.core.EuclideanDistance referenceImplementation = new CIDDTWDistance();
                ITimeSeriesDistance ownImplementation = new ComplexityInvariantDistance(
                                new DynamicTimeWarping(ScalarDistanceUtil.getAbsoluteDistance()),
                                new SquaredBackwardDifferenceComplexity());

                Pair<Double, Double> times = DistanceRefTestUtil.runPerformanceTest(referenceImplementation,
                                ownImplementation, arffFile);
                double refTime = times.getX();
                double ownTime = times.getY();

                System.out.println(String.format("CID. %s dataset - Reference: %.3f ms, own: %.3f ms", fileName,
                                refTime, ownTime));
        }

        // Evaluation.

        @Test
        public void evaluatePerformanceED() throws IOException, TimeSeriesLoadingException {
                weka.core.EuclideanDistance referenceImplementation = new CIDDistance();
                ITimeSeriesDistance ownImplementation = new ComplexityInvariantDistance(new EuclideanDistance(),
                                new StretchingComplexity());
                DistanceRefTestUtil.evaluatePerformance("CID_ED", referenceImplementation, ownImplementation);
        }

        @Test
        public void evaluatePerformanceDTW() throws IOException, TimeSeriesLoadingException {
                weka.core.EuclideanDistance referenceImplementation = new CIDDTWDistance();
                ITimeSeriesDistance ownImplementation = new ComplexityInvariantDistance(
                                new DynamicTimeWarping(ScalarDistanceUtil.getAbsoluteDistance()),
                                new SquaredBackwardDifferenceComplexity());
                DistanceRefTestUtil.evaluatePerformance("CID_DTW", referenceImplementation, ownImplementation);
        }
}
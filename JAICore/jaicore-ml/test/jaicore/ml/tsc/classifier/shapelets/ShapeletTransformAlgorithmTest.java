package jaicore.ml.tsc.classifier.shapelets;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jaicore.ml.tsc.quality_measures.FStat;
import jaicore.ml.tsc.quality_measures.IQualityMeasure;
import jaicore.ml.tsc.shapelets.Shapelet;
import jaicore.ml.tsc.shapelets.search.EarlyAbandonMinimumDistanceSearchStrategy;
import jaicore.ml.tsc.shapelets.search.ExhaustiveMinimumDistanceSearchStrategy;
import jaicore.ml.tsc.util.MathUtil;
import jaicore.ml.tsc.util.TimeSeriesUtil;

/**
 * Unit tests for {@link ShapeletTransformAlgorithm}.
 * 
 * @author Julian Lienen
 *
 */
public class ShapeletTransformAlgorithmTest {
	/**
	 * Maximal delta for asserts with precision.
	 */
	private static final double EPS_DELTA = 0.000001;

	/**
	 * The algorithm object used within the tests.
	 */
	private ShapeletTransformAlgorithm algorithm;

	/**
	 * A commonly used sample instance vector
	 */
	private double[] sampleInstanceVector;

	/**
	 * Hyperparameters
	 */
	private static final int K = 10;
	private static final int NUM_CLUSTERS = 5;
	private static final IQualityMeasure QUALITY_MEASURE = new FStat();
	private static final int SEED = 42;
	private static final boolean CLUSTER = false;

	@Before
	public void setup() {
		algorithm = new ShapeletTransformAlgorithm(K, NUM_CLUSTERS, QUALITY_MEASURE, SEED, CLUSTER);
		sampleInstanceVector = new double[] { 1, 2, 3 };
	}

	/**
	 * See
	 * {@link ShapeletTransformAlgorithm#generateCandidates(double[], int, int)}.
	 */
	@Test
	public void generateCandidatesTest() {
		double[] data = new double[] { 1, 2, 3, 4, 5, 6 };
		int l = 3;

		Set<Shapelet> actResult = ShapeletTransformAlgorithm.generateCandidates(data, l, 0);
		Assert.assertEquals(
				"The number of generated candidates does not match the expected number of generated candidates.", 4,
				actResult.size());

		Shapelet expectedShapelet = new Shapelet(TimeSeriesUtil.zNormalize(new double[] { 1, 2, 3 }, true), 0, 3, 0);
		Assert.assertTrue("The generated shapelets do not match the expected normalized shapelets.",
				actResult.stream().anyMatch(s -> s.equals(expectedShapelet)));

	}

	/**
	 * See {@link ShapeletTransformAlgorithm#findDistances(Shapelet, double[][])}.
	 */
	@Test
	public void findDistancesTest() {
		Shapelet shapelet = new Shapelet(TimeSeriesUtil.zNormalize(new double[] { 1, 2, 3 }, true), 0, 3, 0);
		double[][] dataMatrix = new double[][] { { 4, 2, 4, 6, 5 }, { 2, 2, 2, 2, 2 } };

		// Using the non-optimized version
		List<Double> actResult = algorithm.findDistances(shapelet, dataMatrix);

		Assert.assertEquals("A distance has to be found for each instance!", dataMatrix.length, actResult.size());

		Assert.assertEquals("The first generated distance does not match the expected distance.", 0, actResult.get(0),
				EPS_DELTA);
		Assert.assertEquals("The second generated distance does not match the expected distance.", 2d / 3,
				actResult.get(1), EPS_DELTA); // (1.224744871 * 1.224744871 * 2) / 3,

		// Using the optimized version
		algorithm.setMinDistanceSearchStrategy(new EarlyAbandonMinimumDistanceSearchStrategy(true));
		actResult = algorithm.findDistances(shapelet, dataMatrix);

		Assert.assertEquals("A distance has to be found for each instance!", dataMatrix.length, actResult.size());

		Assert.assertEquals("The first generated distance does not match the expected distance.", 0, actResult.get(0),
				EPS_DELTA);
		Assert.assertEquals("The second generated distance does not match the expected distance.", 2d / 3,
				actResult.get(1), EPS_DELTA); // (1.224744871 * 1.224744871 * 2) / 3,
	}

	/**
	 * See {@link ShapeletTransformAlgorithm#removeSelfSimilar(List)}.
	 */
	@Test
	public void removeSelfSimilarTest() {
		List<Map.Entry<Shapelet, Double>> shapelets = new ArrayList<>();
		shapelets.add(new AbstractMap.SimpleEntry<Shapelet, Double>(new Shapelet(null, 0, 10, 0), 0d));
		shapelets.add(new AbstractMap.SimpleEntry<Shapelet, Double>(new Shapelet(null, 0, 10, 1), 0d));
		shapelets.add(new AbstractMap.SimpleEntry<Shapelet, Double>(new Shapelet(null, 0, 10, 2), 0d));
		shapelets.add(new AbstractMap.SimpleEntry<Shapelet, Double>(new Shapelet(null, 5, 5, 0), 0d));
		shapelets.add(new AbstractMap.SimpleEntry<Shapelet, Double>(new Shapelet(null, 10, 10, 0), 0d));

		Assert.assertEquals("The number of retained shapelets does not match the expected number.", 4,
				ShapeletTransformAlgorithm.removeSelfSimilar(shapelets).size());
	}

	/**
	 * See {@link ShapeletTransformAlgorithm#merge(int, List, List)}.
	 */
	@Test
	public void mergeTest() {
		Map.Entry<Shapelet, Double> testEntry1;
		Map.Entry<Shapelet, Double> testEntry2;

		List<Map.Entry<Shapelet, Double>> shapelets = new ArrayList<>();
		shapelets.add(new AbstractMap.SimpleEntry<Shapelet, Double>(new Shapelet(null, 0, 10, 0), 7d));
		shapelets.add(new AbstractMap.SimpleEntry<Shapelet, Double>(new Shapelet(null, 0, 10, 1), 7d));
		shapelets.add(new AbstractMap.SimpleEntry<Shapelet, Double>(new Shapelet(null, 0, 10, 2), 8d));
		shapelets.add(testEntry2 = new AbstractMap.SimpleEntry<Shapelet, Double>(new Shapelet(null, 5, 5, 0), 2d));
		shapelets.add(new AbstractMap.SimpleEntry<Shapelet, Double>(new Shapelet(null, 10, 10, 0), 1d));

		List<Map.Entry<Shapelet, Double>> newShapelets = new ArrayList<>();
		newShapelets.add(testEntry1 = new AbstractMap.SimpleEntry<Shapelet, Double>(new Shapelet(null, 0, 10, 0), 5d));
		newShapelets.add(new AbstractMap.SimpleEntry<Shapelet, Double>(new Shapelet(null, 0, 10, 1), 2d));
		newShapelets.add(new AbstractMap.SimpleEntry<Shapelet, Double>(new Shapelet(null, 0, 10, 2), 3d));
		newShapelets.add(new AbstractMap.SimpleEntry<Shapelet, Double>(new Shapelet(null, 5, 5, 0), 0d));
		newShapelets.add(new AbstractMap.SimpleEntry<Shapelet, Double>(new Shapelet(null, 10, 10, 0), 0d));

		int k = 5; // Retain k elements

		List<Map.Entry<Shapelet, Double>> actResult = ShapeletTransformAlgorithm.merge(k, shapelets, newShapelets);

		Assert.assertEquals("The number of retained elements does not match the expected number.", k, actResult.size());
		Assert.assertTrue("The retained shapelets do not contain an element which should be contained.",
				actResult.contains(testEntry1));
		Assert.assertFalse("The retained shapelets contain an element which sould not be contained.",
				actResult.contains(testEntry2));
	}

	/**
	 * See {@link ShapeletTransformAlgorithm#zNormalize(double[], boolean)}.
	 */
	@Test
	public void zNormalizeTest() {
		double[] expectedResult = new double[] { -1, 0, +1 };

		double[] actResult = TimeSeriesUtil.zNormalize(sampleInstanceVector, true);
		Assert.assertArrayEquals("The normalized values of the vector do not match the expected results.",
				expectedResult, actResult, EPS_DELTA);
	}

	/**
	 * See
	 * {@link ShapeletTransformAlgorithm#getMinimumDistanceAmongAllSubsequences(Shapelet, double[])}
	 * and
	 * {@link ShapeletTransformAlgorithm#getMinimumDistanceAmongAllSubsequencesOptimized(Shapelet, double[])
	 */
	@Test
	public void getMinimumDistanceAmongAllSequencesOptimizedTest() {
		double[] matrix = new double[] { 4, 1, 2, 4, 6, 5 };

		Shapelet shapelet = new Shapelet(TimeSeriesUtil.zNormalize(sampleInstanceVector, true), 0, 3, 0);

		EarlyAbandonMinimumDistanceSearchStrategy optSearchStrategy = new EarlyAbandonMinimumDistanceSearchStrategy(
				true);
		ExhaustiveMinimumDistanceSearchStrategy unoptSearchStrategy = new ExhaustiveMinimumDistanceSearchStrategy(true);

		double actResult = unoptSearchStrategy.findMinimumDistance(shapelet, matrix);

		Assert.assertEquals("The result of the exhaustive search strategy does not match the expected result.", 0.0,
				actResult, EPS_DELTA);

		Assert.assertEquals("The result of the optimimum search strategy does not match the expected result.",
				actResult,
				optSearchStrategy.findMinimumDistance(shapelet, matrix),
				EPS_DELTA);

		Shapelet shapelet2 = new Shapelet(TimeSeriesUtil.zNormalize(new double[] { 1, 4, 2 }, true), 0, 3, 0);
		Assert.assertEquals("The result of the exhaustive search strategy does not match the expected result.",
				0.024025991917379048d, unoptSearchStrategy.findMinimumDistance(shapelet2, matrix), EPS_DELTA);
	}

	/**
	 * See {@link MathUtil#singleSquaredEuclideanDistance(double[], double[])}.
	 */
	@Test
	public void squareEuclideanDistanceTest() {
		double[] vector = new double[] { 4, 2, 6 };
		double[] vector2 = new double[] { 2, 4, 6 };

		Assert.assertEquals(
				"The calculation of the single squared euclidean distance does not match the expected result.", 8d,
				MathUtil.singleSquaredEuclideanDistance(vector, vector2), EPS_DELTA);
	}

	/**
	 * See {@link
	 * ShapeletTransformAlgorithm#getMinimumDistanceAmongAllSubsequences(Shapelet,
	 * double[])} and {@link
	 * ShapeletTransformAlgorithm#getMinimumDistanceAmongAllSubsequencesOptimized(Shapelet,
	 * double[])
	 */
	@Test
	public void getMinimumDistanceAmongAllSequencesOptimizedTest2() {
		double[] matrix = new double[] { 4, 3, 6, 9, 23, 1 };

		Shapelet shapelet = new Shapelet(TimeSeriesUtil.zNormalize(sampleInstanceVector, true), 0, 3, 0);

		EarlyAbandonMinimumDistanceSearchStrategy optSearchStrategy = new EarlyAbandonMinimumDistanceSearchStrategy(
				true);
		ExhaustiveMinimumDistanceSearchStrategy unoptSearchStrategy = new ExhaustiveMinimumDistanceSearchStrategy(true);

		double oldResult = unoptSearchStrategy.findMinimumDistance(shapelet, matrix);
		double oldOptimizedResult = optSearchStrategy.findMinimumDistance(shapelet,
				matrix);

		Assert.assertEquals(
				"The minimum distance of the unoptimzied search strategy does not match the result of the optimzed search strategy.",
				oldResult, oldOptimizedResult, EPS_DELTA);
	}
}

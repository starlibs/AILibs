package jaicore.ml.tsc.classifier.shapelets;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import jaicore.ml.tsc.quality_measures.FStat;
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
	 * See
	 * {@link ShapeletTransformAlgorithm#generateCandidates(double[], int, int)}.
	 */
	@Test
	public void generateCandidatesTest() {
		double[] data = new double[] { 1, 2, 3, 4, 5, 6 };
		int l = 3;

		Set<Shapelet> actResult = ShapeletTransformAlgorithm.generateCandidates(data, l, 0);
		Assert.assertEquals(4, actResult.size());

		Shapelet expectedShapelet = new Shapelet(TimeSeriesUtil.zNormalize(new double[] { 1, 2, 3 }, true), 0, 3, 0);
		Assert.assertTrue(actResult.stream().anyMatch(s -> s.equals(expectedShapelet)));

	}

	/**
	 * See {@link ShapeletTransformAlgorithm#findDistances(Shapelet, double[][])}.
	 */
	@Test
	public void findDistancesTest() {
		Shapelet shapelet = new Shapelet(TimeSeriesUtil.zNormalize(new double[] { 1, 2, 3 }, true), 0, 3, 0);
		double[][] dataMatrix = new double[][] { { 4, 2, 4, 6, 5 }, { 2, 2, 2, 2, 2 } };

		// Using the non-optimized version
		ShapeletTransformAlgorithm stAlgorithm = new ShapeletTransformAlgorithm(10, 5, new FStat(), 42, false);

		List<Double> actResult = stAlgorithm.findDistances(shapelet, dataMatrix);

		Assert.assertEquals("A distance has to be found for each instance!", dataMatrix.length, actResult.size());

		Assert.assertEquals(0, actResult.get(0), EPS_DELTA);
		Assert.assertEquals(2d / 3, actResult.get(1), EPS_DELTA); // (1.224744871 * 1.224744871 * 2) / 3,

		// Using the optimized version
		stAlgorithm.setMinDistanceSearchStrategy(new EarlyAbandonMinimumDistanceSearchStrategy(true));
		actResult = stAlgorithm.findDistances(shapelet, dataMatrix);

		Assert.assertEquals("A distance has to be found for each instance!", dataMatrix.length, actResult.size());

		Assert.assertEquals(0, actResult.get(0), EPS_DELTA);
		Assert.assertEquals(2d / 3, actResult.get(1), EPS_DELTA); // (1.224744871 * 1.224744871 * 2) / 3,
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

		Assert.assertEquals(4, ShapeletTransformAlgorithm.removeSelfSimilar(shapelets).size());
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

		Assert.assertEquals(k, actResult.size());
		Assert.assertTrue(actResult.contains(testEntry1));
		Assert.assertFalse(actResult.contains(testEntry2));
	}

	/**
	 * See {@link ShapeletTransformAlgorithm#zNormalize(double[], boolean)}.
	 */
	@Test
	public void zNormalizeTest() {
		double[] vector = new double[] { 1, 2, 3 };
		// INDArray expectedResult = Nd4j.create(new double[] { -1.224744871, 0,
		// +1.224744871 });
		double[] expectedResult = new double[] { -1, 0, +1 };

		double[] actResult = TimeSeriesUtil.zNormalize(vector, true);
		Assert.assertArrayEquals(expectedResult, actResult, EPS_DELTA);

		double[] array = new double[] { 2, 4, 6 };

		System.out.println(Arrays.toString(TimeSeriesUtil.zNormalize(array, true)));
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

		double[] vector = new double[] { 1, 2, 3 };
		Shapelet shapelet = new Shapelet(TimeSeriesUtil.zNormalize(vector, true), 0, 3, 0);

		EarlyAbandonMinimumDistanceSearchStrategy optSearchStrategy = new EarlyAbandonMinimumDistanceSearchStrategy(
				true);
		ExhaustiveMinimumDistanceSearchStrategy unoptSearchStrategy = new ExhaustiveMinimumDistanceSearchStrategy(true);

		double actResult = unoptSearchStrategy.findMinimumDistance(shapelet, matrix);

		Assert.assertEquals(0.0, actResult, EPS_DELTA);

		Assert.assertEquals(actResult,
				optSearchStrategy.findMinimumDistance(shapelet, matrix),
				EPS_DELTA);
	}

	/**
	 * See
	 * {@link ShapeletTransformAlgorithm#singleSquaredEuclideanDistance(double[], double[])}.
	 */
	@Test
	public void squareEuclideanDistanceTest() {
		double[] vector = new double[] { 4, 2, 6 };
		double[] vector2 = new double[] { 2, 4, 6 };

		Assert.assertEquals(8d, MathUtil.singleSquaredEuclideanDistance(vector, vector2), EPS_DELTA);
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
		// INDArray matrix = Nd4j.create(new double[][] { { 4, 2, 4, 6, 5, } });

		double[] vector = new double[] { 1, 2, 3 };
		Shapelet shapelet = new Shapelet(TimeSeriesUtil.zNormalize(vector, true), 0, 3, 0);
		System.out.println("Normalized vector: " + Arrays.toString(shapelet.getData()));

		EarlyAbandonMinimumDistanceSearchStrategy optSearchStrategy = new EarlyAbandonMinimumDistanceSearchStrategy(
				true);
		ExhaustiveMinimumDistanceSearchStrategy unoptSearchStrategy = new ExhaustiveMinimumDistanceSearchStrategy(true);

		double oldResult = unoptSearchStrategy.findMinimumDistance(shapelet, matrix);
		double oldOptimizedResult = optSearchStrategy.findMinimumDistance(shapelet,
				matrix);

		Assert.assertEquals(oldResult, oldOptimizedResult, EPS_DELTA);
	}
}

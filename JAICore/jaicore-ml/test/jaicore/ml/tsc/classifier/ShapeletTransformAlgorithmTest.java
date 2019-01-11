package jaicore.ml.tsc.classifier;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.tsc.classifier.ShapeletTransformAlgorithm.Shapelet;
import junit.framework.Assert;

public class ShapeletTransformAlgorithmTest {

	private static final double EPS_DELTA = 0.000001;

	@Test
	public void generateCandidatesTest() {
		INDArray data = Nd4j.create(new double[] { 1, 2, 3, 4, 5, 6 }).ravel();
		int l = 3;

		Set<Shapelet> actResult = ShapeletTransformAlgorithm.generateCandidates(data, l, 0);
		Assert.assertEquals(4, actResult.size());

		Shapelet expectedShapelet = new Shapelet(Nd4j.create(new double[] { 1, 2, 3 }), 0, 3, 0);
		Assert.assertTrue(actResult.stream().anyMatch(s -> s.equals(expectedShapelet)));

	}

	@Test
	public void findDistancesTest() {
		Shapelet shapelet = new Shapelet(Nd4j.create(new double[] { 1, 2, 3 }), 0, 3, 0);
		// INDArray dataMatrix = Nd4j.create(new double[][] { { 4, 1, 2, 3, 5 }, { 2, 2,
		// 2, 2, 2 } });
		INDArray dataMatrix = Nd4j.create(new double[][] { { 4, 2, 4, 6, 5 }, { 2, 2, 2, 2, 2 } });

		List<Double> actResult = ShapeletTransformAlgorithm.findDistances(shapelet, dataMatrix);

		Assert.assertEquals("A distance has to be found for each instance!", dataMatrix.shape()[0], actResult.size());

		Assert.assertEquals(0, actResult.get(0), EPS_DELTA);
		Assert.assertEquals(2d / 3, actResult.get(1), EPS_DELTA); // (1.224744871 * 1.224744871 * 2) / 3,
	}

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

	@Test
	public void zNormalizeTest() {
		INDArray vector = Nd4j.create(new double[] { 1, 2, 3 });
		// INDArray expectedResult = Nd4j.create(new double[] { -1.224744871, 0,
		// +1.224744871 });
		INDArray expectedResult = Nd4j.create(new double[] { -1, 0, +1 });

		INDArray actResult = ShapeletTransformAlgorithm.zNormalize(vector);
		Assert.assertTrue(expectedResult.equalsWithEps(actResult, EPS_DELTA));

		INDArray array = Nd4j.create(new double[] { 2, 4, 6 });
		System.out.println(array.stdNumber(false).doubleValue());

		System.out.println(ShapeletTransformAlgorithm.zNormalize(array));
	}

	@Test
	public void getMinimumDistanceAmongAllSequencesOptimizedTest() {
		INDArray matrix = Nd4j.create(new double[][] { { 4, 2, 4, 6, 5 } });

		INDArray vector = Nd4j.create(new double[] { 1, 2, 3 });
		Shapelet shapelet = new Shapelet(vector, 0, 3, 0);

		double actResult = ShapeletTransformAlgorithm.getMinimumDistanceAmongAllSubsequences(shapelet, matrix);

		Assert.assertEquals(0.0, actResult, EPS_DELTA);

		Assert.assertEquals(actResult,
				ShapeletTransformAlgorithm.getMinimumDistanceAmongAllSubsequencesOptimized(shapelet, matrix),
				EPS_DELTA);
	}

	@Test
	public void sortIndexesTest() {
		INDArray vector = Nd4j.create(new double[] { 4, 2, 6 });
		INDArray vector2 = Nd4j.create(new double[] { 2, 4, 6 });

		List<Integer> result1 = ShapeletTransformAlgorithm.sortIndexes(vector, true);
		List<Integer> result1Inv = ShapeletTransformAlgorithm.sortIndexes(vector, false);

		Assert.assertEquals(Arrays.asList(1, 0, 2), result1);
		Assert.assertEquals(Arrays.asList(2, 0, 1), result1Inv);

		List<Integer> result2 = ShapeletTransformAlgorithm.sortIndexes(vector2, true);

		Assert.assertEquals(Arrays.asList(0, 1, 2), result2);
	}

	@Test
	public void squareEuclideanDistanceTest() {
		INDArray vector = Nd4j.create(new double[] { 4, 2, 6 });
		INDArray vector2 = Nd4j.create(new double[] { 2, 4, 6 });

		Assert.assertEquals(8d, ShapeletTransformAlgorithm.singleSquaredEuclideanDistance(vector, vector2),
				EPS_DELTA);
	}

	@Test
	public void getMinimumDistanceAmongAllSequencesOptimizedTest2() {
		INDArray matrix = Nd4j.create(new double[][] { { 4, 3, 6, 9 } });
		// INDArray matrix = Nd4j.create(new double[][] { { 4, 2, 4, 6, 5, } });

		INDArray vector = Nd4j.create(new double[] { 1, 2, 3 });
		Shapelet shapelet = new Shapelet(vector, 0, 3, 0);

		double oldResult = ShapeletTransformAlgorithm.getMinimumDistanceAmongAllSubsequences(shapelet, matrix);
		System.out.println("oldResult: " + 3 * oldResult);
		double oldOptimizedResult = ShapeletTransformAlgorithm.getMinimumDistanceAmongAllSubsequencesOptimized(shapelet,
				matrix);

		System.out.println("=============================================");
		double actResult = ShapeletTransformAlgorithm.getMinimumDistanceAmongAllSubsequencesOptimized2(shapelet,
				matrix);

		Assert.assertEquals(oldResult, oldOptimizedResult, EPS_DELTA);
		Assert.assertEquals(oldOptimizedResult, actResult, EPS_DELTA);
		// Assert.assertEquals(0, actResult, EPS_DELTA);

		System.out.println(actResult);
	}
}

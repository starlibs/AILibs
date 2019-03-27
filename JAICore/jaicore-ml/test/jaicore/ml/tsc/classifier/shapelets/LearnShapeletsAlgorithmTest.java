package jaicore.ml.tsc.classifier.shapelets;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import jaicore.ml.tsc.util.MathUtil;
import junit.framework.Assert;

/**
 * Unit tests for {@link LearnShapeletsAlgorithm}.
 * 
 * @author Julian Lienen
 *
 */
public class LearnShapeletsAlgorithmTest {
	/**
	 * Maximal delta for asserts with precision.
	 */
	private static final double EPS_DELTA = 0.00001;

	/**
	 * The algorithm object used within the tests.
	 */
	private LearnShapeletsAlgorithm algorithm;

	/**
	 * Shapelets used within the tests.
	 */
	private double[][][] S;

	/**
	 * Rule for expected exceptions.
	 */
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	/**
	 * Hyperparameters
	 */
	private static final int K = 1;
	private static final double LEARNING_RATE = 0.01;
	private static final double REGULARIZATION = 0.01;
	private static final int SCALE_R = 2;
	private static final double MIN_SHAPELET_LENGTH_PCT = 0.1;
	private static final int MAX_ITER = 300;
	private static final int SEED = 42;

	/**
	 * Setting up objects used within the tests.
	 */
	@Before
	public void setup() {
		algorithm = new LearnShapeletsAlgorithm(K, LEARNING_RATE, REGULARIZATION, SCALE_R, MIN_SHAPELET_LENGTH_PCT,
				MAX_ITER, SEED);
		S = new double[][][] { { { 1, 2, 3 } } };
	}

	/**
	 * See {@link MathUtil#sigmoid(double)}.
	 */
	@Test
	public void sigmoidTest() {
		final double z = 0.5;
		final double expected = 0.6224593312018545646389;
		Assert.assertEquals("The sigmoid calculation delivers a wrong value.", expected, MathUtil.sigmoid(z),
				EPS_DELTA);
	}

	/**
	 * See {@link LearnShapeletsAlgorithm#getNumberOfSegments(int, int, int)}.
	 */
	@Test
	public void getNumberOfSegmentsTest() {
		final int Q = 30;
		final int r = 5;
		final int minShapeLength = 2;

		// Actual implementation differs from paper's version due to index shift
		// correction
		Assert.assertEquals("The calculated number of segments does not match the expected number of segments.",
				30 - (5 + 1) * 2, LearnShapeletsAlgorithm.getNumberOfSegments(Q, minShapeLength, r));
	}

	/**
	 * See
	 * {@link LearnShapeletsAlgorithm#calculateD(double[][][], int, int, double[], int, int)}.
	 */
	@Test
	public void calculateDTest() {
		final int minShapeLength = 3; // Length of 2 combined with r = 0
		final int r = 0; // Take first S array
		final double[] instance = new double[] { 1, 2, 3 };
		final int j = 0; // Start with first index

		Assert.assertEquals("The calculated distance of the given instance does not match the expected distance.", 0d,
				LearnShapeletsAlgorithm.calculateD(S, minShapeLength, r, instance, 0, j), EPS_DELTA);

		final double[] instance2 = new double[] { 2, 3, 4 }; // Shifted by one => Differs 1 per position

		final double expected = 3d / 3d;
		
		Assert.assertEquals("The calculated distance of the given instance does not match the expected distance.",
				expected, LearnShapeletsAlgorithm.calculateD(S, minShapeLength, r, instance2, 0, j), EPS_DELTA);
	}
	
	/**
	 * See
	 * {@link LearnShapeletsAlgorithm#calculateM_hat(double[][][], int, int, double[], int, int, double)}.
	 */
	@Test
	public void calculateM_hatTest() {
		final int minShapeLength = 3; // Length of 2 combined with r = 0
		final int r = 0; // Take first S array
		final double[] instance = new double[] { 1, 2, 3, 4 };
		final double alpha = -1d;

		// getNumberOfSegments(Q=4, minShapeLength=3, r=0) = 1
		Assert.assertEquals("The calculated soft minimum distance does not match the expected distance.", 0d,
				LearnShapeletsAlgorithm.calculateM_hat(S, minShapeLength, r, instance, 0, instance.length, alpha));

		final double[] instance2 = new double[] { 1, 2, 3 };
		// getNumberOfSegments(Q=3, minShapeLength=3, r=0) = 0
		Assert.assertEquals("The calculated soft minimum distance does not match the expected distance.", 0d,
				LearnShapeletsAlgorithm.calculateM_hat(S, minShapeLength, r, instance2, 0, instance2.length, alpha));
	}

	/**
	 * See
	 * {@link LearnShapeletsAlgorithm#shuffleAccordingToAlternatingClassScheme(List, int[], Random)}.
	 */
	@Test
	public void shuffleAccordingToSchemeTest() {
		int[] targets = new int[] { 2, 2, 0, 1, 1, 2, 0, 1 };

		List<Integer> indices = IntStream.range(0, targets.length).boxed().collect(Collectors.toList());

		Random random = new Random(SEED);
		algorithm.setC(3);

		List<Integer> result = algorithm.shuffleAccordingToAlternatingClassScheme(indices, targets, random);
		Assert.assertEquals("The result indices size does not match the expected size.", indices.size(), result.size());
		Assert.assertTrue("The calculated indices does not contain each original index.",
				result.stream().allMatch(i -> indices.contains(i)));
		Assert.assertTrue("The first element is not member of the first class.", targets[result.get(0)] == 0);
		
		// Check thrown exception
		exception.expect(IllegalArgumentException.class);
		algorithm.shuffleAccordingToAlternatingClassScheme(Arrays.asList(1, 2, 3), new int[] { 0, 1 }, random);
	}
}

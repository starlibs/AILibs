package ai.libs.jaicore.ml.weka.classification.singlelabel.timeseries.learner.shapelets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

import ai.libs.jaicore.ml.classification.singlelabel.timeseries.util.MathUtil;

/**
 * Unit tests for {@link LearnShapeletsLearningAlgorithm}.
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
	private LearnShapeletsLearningAlgorithm algorithm;

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
	@BeforeEach
	public void setup() {
		LearnShapeletsClassifier classifier = new LearnShapeletsClassifier(K, LEARNING_RATE, REGULARIZATION, SCALE_R, MIN_SHAPELET_LENGTH_PCT, MAX_ITER, SEED);
		this.algorithm = classifier.getLearningAlgorithm(null);
		this.S = new double[][][] { { { 1, 2, 3 } } };
	}

	/**
	 * See {@link MathUtil#sigmoid(double)}.
	 */
	@Test
	public void sigmoidTest() {
		final double z = 0.5;
		final double expected = 0.6224593312018545646389;
		assertEquals(expected, MathUtil.sigmoid(z), EPS_DELTA, "The sigmoid calculation delivers a wrong value.");
	}

	/**
	 * See {@link LearnShapeletsLearningAlgorithm#getNumberOfSegments(int, int, int)}.
	 */
	@Test
	public void getNumberOfSegmentsTest() {
		final int Q = 30;
		final int r = 5;
		final int minShapeLength = 2;

		// Actual implementation differs from paper's version due to index shift correction
		assertEquals(30 - (5 + 1) * 2, LearnShapeletsLearningAlgorithm.getNumberOfSegments(Q, minShapeLength, r), "The calculated number of segments does not match the expected number of segments.");
	}

	/**
	 * See
	 * {@link LearnShapeletsLearningAlgorithm#calculateD(double[][][], int, int, double[], int, int)}.
	 */
	@Test
	public void calculateDTest() {
		final int minShapeLength = 3; // Length of 2 combined with r = 0
		final int r = 0; // Take first S array
		final double[] instance = new double[] { 1, 2, 3 };
		final int j = 0; // Start with first index

		assertEquals(0d, LearnShapeletsLearningAlgorithm.calculateD(this.S, minShapeLength, r, instance, 0, j), EPS_DELTA, "The calculated distance of the given instance does not match the expected distance.");

		final double[] instance2 = new double[] { 2, 3, 4 }; // Shifted by one => Differs 1 per position

		final double expected = 3d / 3d;

		assertEquals(expected, LearnShapeletsLearningAlgorithm.calculateD(this.S, minShapeLength, r, instance2, 0, j), EPS_DELTA, "The calculated distance of the given instance does not match the expected distance.");
	}

	/**
	 * See
	 * {@link LearnShapeletsLearningAlgorithm#calculateMHat(double[][][], int, int, double[], int, int, double)}.
	 */
	@Test
	public void calculateM_hatTest() {
		final int minShapeLength = 3; // Length of 2 combined with r = 0
		final int r = 0; // Take first S array
		final double[] instance = new double[] { 1, 2, 3, 4 };
		final double alpha = -1d;

		// getNumberOfSegments(Q=4, minShapeLength=3, r=0) = 1
		assertEquals(0d, LearnShapeletsLearningAlgorithm.calculateMHat(this.S, minShapeLength, r, instance, 0, instance.length, alpha), "The calculated soft minimum distance does not match the expected distance.");

		final double[] instance2 = new double[] { 1, 2, 3 };
		// getNumberOfSegments(Q=3, minShapeLength=3, r=0) = 0
		assertEquals(0d, LearnShapeletsLearningAlgorithm.calculateMHat(this.S, minShapeLength, r, instance2, 0, instance2.length, alpha), "The calculated soft minimum distance does not match the expected distance.");
	}

	/**
	 * See
	 * {@link LearnShapeletsLearningAlgorithm#shuffleAccordingToAlternatingClassScheme(List, int[], Random)}.
	 */
	@Test
	public void shuffleAccordingToSchemeTest() {
		int[] targets = new int[] { 2, 2, 0, 1, 1, 2, 0, 1 };

		List<Integer> indices = IntStream.range(0, targets.length).boxed().collect(Collectors.toList());

		Random random = new Random(SEED);
		this.algorithm.setC(3);

		List<Integer> result = this.algorithm.shuffleAccordingToAlternatingClassScheme(indices, targets, random);
		assertEquals(indices.size(), result.size(), "The result indices size does not match the expected size.");
		assertTrue(result.stream().allMatch(i -> indices.contains(i)), "The calculated indices does not contain each original index.");
		assertEquals(0, targets[result.get(0)], "The first element is not member of the first class.");

		// Check thrown exception
		List<Integer> l1 = Arrays.asList(1, 2, 3);
		int[] a1 = new int[] { 0, 1 };
		assertThrows(IllegalArgumentException.class, () -> this.algorithm.shuffleAccordingToAlternatingClassScheme(l1, a1, random));
	}
}

package jaicore.ml.tsc.classifier.neighbors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the
 * {@link jaicore.ml.tsc.classifier.neighbors.ShotgunEnsembleLearnerAlgorithm}
 * implementation.
 *
 * @author fischor
 */
public class ShotgunEnsembleClassifierTest {

	ShotgunEnsembleLearnerAlgorithm algorithm;

	ShotgunEnsembleClassifier shotgunEnsembleClassifier;


	// Set up model and algorithm.
	private int minWindowLength = 3;
	private int maxWindowLength = 6;
	private boolean meanNormalization = true;

	@Before
	public void setUp() {

		double factor = 0.95;
		this.shotgunEnsembleClassifier = new ShotgunEnsembleClassifier(this.minWindowLength, this.maxWindowLength, this.meanNormalization, factor);
		this.algorithm = this.shotgunEnsembleClassifier.getLearningAlgorithm(null);
	}

	@Test
	public void testCorrectnessCalculateWindowLengthPredictionsOnInstance() {
		// TODO
	}

	@Test
	public void testMostFrequentLabelFromWindowLengthPredicitons() {
		Map<Integer, Integer> windowLengthPredicitions = new HashMap<>();
		windowLengthPredicitions.put(2, 1);
		windowLengthPredicitions.put(3, 2);
		windowLengthPredicitions.put(4, 2);
		windowLengthPredicitions.put(5, 2);
		windowLengthPredicitions.put(6, 1);
		windowLengthPredicitions.put(7, 2);
		windowLengthPredicitions.put(8, 3);
		windowLengthPredicitions.put(9, 3);
		int mostFrequentlabel = this.shotgunEnsembleClassifier
				.mostFrequentLabelFromWindowLengthPredicitions(windowLengthPredicitions);
		int expectation = 2;
		assertEquals(expectation, mostFrequentlabel);
	}

	@Test
	public void testCorrectnessForCalculateWindowLengthPredictionsOnDataset() {
		// TODO
	}

	@Test
	public void testCorrectnessForMostFrequentLabelsFromWindowLengthPredicitions() {
		// Input.
		Map<Integer, List<Integer>> windowLengthPredicitions = new HashMap<>();
		List<Integer> list1 = Arrays.asList(1, 2, 3, 1, 1);
		List<Integer> list2 = Arrays.asList(3, 2, 3, 1, 1);
		List<Integer> list3 = Arrays.asList(2, 1, 3, 2, 1);
		List<Integer> list4 = Arrays.asList(1, 2, 3, 3, 1);
		windowLengthPredicitions.put(1, list1);
		windowLengthPredicitions.put(2, list1);
		windowLengthPredicitions.put(3, list1);
		windowLengthPredicitions.put(4, list1);
		// Expectation.
		int[] expectation = { 1, 2, 3, 1, 1 };

		List<Integer> mostFrequentLabels = this.shotgunEnsembleClassifier
				.mostFrequentLabelsFromWindowLengthPredicitions(windowLengthPredicitions);

		assertArrayEquals(expectation, mostFrequentLabels.stream().mapToInt(i -> i).toArray());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessOfConstructorForInvalidFactors1() {
		// Too low factor.
		double factor = -1e5;
		new ShotgunEnsembleClassifier(this.minWindowLength, this.maxWindowLength, this.meanNormalization, factor);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessOfConstructorForInvalidFactors2() {
		// Too low factor.
		double factor = 0;
		new ShotgunEnsembleClassifier(this.minWindowLength, this.maxWindowLength, this.meanNormalization, factor);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessOfConstructorForInvalidFactors3() {
		// Too high factor.
		double factor = 1 + 1e5;
		new ShotgunEnsembleClassifier(this.minWindowLength, this.maxWindowLength, this.meanNormalization, factor);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessOfConstructorForInvalidFactors4() {
		// Too high factor.
		double factor = 2;
		new ShotgunEnsembleClassifier(this.minWindowLength, this.maxWindowLength, this.meanNormalization, factor);
	}

	@Test
	public void testRobustnessOfConstructorForValidFactors1() {
		// Minimal factor.
		double factor = Double.MIN_VALUE;
		new ShotgunEnsembleClassifier(this.minWindowLength, this.maxWindowLength, this.meanNormalization, factor);
	}

	@Test
	public void testRobustnessOfConstructorForValidFactors2() {
		// Some valid factor in between.
		double factor = 0.5;
		new ShotgunEnsembleClassifier(this.minWindowLength, this.maxWindowLength, this.meanNormalization, factor);
	}

	@Test
	public void testRobustnessOfConstructorForValidFactors3() {
		// Maximal factor.
		double factor = 1;
		new ShotgunEnsembleClassifier(this.minWindowLength, this.maxWindowLength, this.meanNormalization, factor);
	}
}
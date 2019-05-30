package jaicore.ml.tsc.classifier.neighbors;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;

/**
 * Test suite for the
 * {@link jaicore.ml.tsc.classifier.neighbors.ShotgunEnsembleLearnerAlgorithm}
 * implementation.
 *
 * @author fischor
 */
public class ShotgunEnsembleAlgorithmTest {

	/**
	 * Dataset containing the values <code>
	 * {
	 *  { 0.1, 0.1, 0.8, 0.1 },
	 *  { 0.25, 0.2, 0.25, 0.2 },
	 *  { 0.1, 0.2, 0.3, 0.5 },
	 *  { 0.15, 0.14, 0.1, 0.1 }
	 * }
	 * </code> after set up.
	 */
	TimeSeriesDataset dataset;

	ShotgunEnsembleClassifier model;

	ShotgunEnsembleLearnerAlgorithm algorithm;

	private int minWindowLength = 4;
	private int maxWindowLength = 6;
	private boolean meanNormalization = true;

	@Before
	public void setUp() {
		// Set up dataset.
		double data[][] = { { 0.1, 0.1, 0.8, 0.1 }, { 0.25, 0.2, 0.25, 0.2 }, { 0.1, 0.2, 0.3, 0.5 },
				{ 0.15, 0.14, 0.1, 0.1 } };
		int[] targets = { 1, 2, 1, 2 };
		ArrayList<double[][]> values = new ArrayList<>(1);
		values.add(data);
		this.dataset = new TimeSeriesDataset(values, targets);

		// Create algorithm
		this.model = new ShotgunEnsembleClassifier(this.minWindowLength, this.maxWindowLength, this.meanNormalization, 0.5);

		// Create model.
		double factor = 1;
		this.algorithm = this.model.getLearningAlgorithm(null);
	}

	@Test
	public void testCorrectness()
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		// Create algorithm.
		int minWindowLength = 3;
		int maxWindowLength = 4;
		boolean meanNormalization = true;

		// Create model.
		double factor = 1;
		ShotgunEnsembleClassifier model = new ShotgunEnsembleClassifier(minWindowLength, maxWindowLength, meanNormalization, factor);
		ShotgunEnsembleLearnerAlgorithm algorithm = model.getLearningAlgorithm(this.dataset);

		// Training.
		algorithm.call();

		// Check model to contains (3, 2) and (4, 2).
		assertEquals(2, model.windows.size());
		for (Pair<Integer, Integer> window : model.windows) {
			switch (window.getY()) {
			case 3:
				assertEquals(2, (int) window.getX());
				break;
			case 4:
				assertEquals(2, (int) window.getX());
				break;
			default:
				assertEquals(1, 2);
			}
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForConstructorWithInvalidWindowLenghts1() {
		// Too low minWindowLength.
		int minWindowLength = 0;
		int maxWindowLength = 3;
		new ShotgunEnsembleClassifier(minWindowLength, maxWindowLength, true, 0).getLearningAlgorithm(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForConstructorWithInvalidWindowLenghts2() {
		// Too low maxWindowLength.
		int minWindowLength = 3;
		int maxWindowLength = 0;
		new ShotgunEnsembleClassifier(minWindowLength, maxWindowLength, true, 0).getLearningAlgorithm(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForConstructorWithInvalidWindowLenghts3() {
		// Too low maxWindowLength.
		int minWindowLength = 3;
		int maxWindowLength = 0;
		new ShotgunEnsembleClassifier(minWindowLength, maxWindowLength, true, 0).getLearningAlgorithm(null);
	}

	@Test
	public void testRobustnessForCallingWithoutModelSet() throws AlgorithmException {
		// Call algorithm without model set.
		ShotgunEnsembleLearnerAlgorithm algorithm = this.model.getLearningAlgorithm(this.dataset);
		algorithm.call();
	}
}
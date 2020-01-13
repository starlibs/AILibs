package ai.libs.jaicore.ml.classification.singlelabel.timeseries.learner.neighbors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.junit.Before;
import org.junit.Test;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.TimeSeriesDataset2;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.learner.neighbors.ShotgunEnsembleClassifier;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.learner.neighbors.ShotgunEnsembleLearnerAlgorithm;

/**
 * Test suite for the
 * {@link ai.libs.jaicore.ml.classification.singlelabel.timeseries.learner.neighbors.ShotgunEnsembleLearnerAlgorithm}
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
	TimeSeriesDataset2 dataset;

	ShotgunEnsembleClassifier model;

	ShotgunEnsembleLearnerAlgorithm algorithm;

	private int minWindowLength = 4;
	private int maxWindowLength = 6;
	private boolean meanNormalization = true;

	@Before
	public void setUp() {
		// Set up dataset.
		double[][] data = { { 0.1, 0.1, 0.8, 0.1 }, { 0.25, 0.2, 0.25, 0.2 }, { 0.1, 0.2, 0.3, 0.5 }, { 0.15, 0.14, 0.1, 0.1 } };
		int[] targets = { 1, 2, 1, 2 };
		ArrayList<double[][]> values = new ArrayList<>(1);
		values.add(data);
		this.dataset = new TimeSeriesDataset2(values, targets);

		// Create algorithm
		this.model = new ShotgunEnsembleClassifier(this.minWindowLength, this.maxWindowLength, this.meanNormalization, 0.5);

		// Create model.
		this.algorithm = this.model.getLearningAlgorithm(null);
	}

	@Test
	public void testCorrectness() throws AlgorithmException {
		// Create algorithm.
		int lMinWindowLength = 3;
		int lMaxWindowLength = 4;
		boolean lMeanNormalization = true;

		// Create model.
		double factor = 1;
		ShotgunEnsembleClassifier lModel = new ShotgunEnsembleClassifier(lMinWindowLength, lMaxWindowLength, lMeanNormalization, factor);
		ShotgunEnsembleLearnerAlgorithm lAlgorithm = lModel.getLearningAlgorithm(this.dataset);

		// Training.
		lAlgorithm.call();

		// Check model to contains (3, 2) and (4, 2).
		assertEquals(2, lModel.windows.size());
		for (Pair<Integer, Integer> window : lModel.windows) {
			switch (window.getY()) {
			case 3:
				assertEquals(2, (int) window.getX());
				break;
			case 4:
				assertEquals(2, (int) window.getX());
				break;
			default:
				fail("The default case has not been covered!");
			}
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForConstructorWithInvalidWindowLenghts1() {
		new ShotgunEnsembleClassifier(0, 3, true, 0).getLearningAlgorithm(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForConstructorWithInvalidWindowLenghts2() {
		new ShotgunEnsembleClassifier(3, 0, true, 0).getLearningAlgorithm(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRobustnessForConstructorWithInvalidWindowLenghts3() {
		new ShotgunEnsembleClassifier(3, 0, true, 0).getLearningAlgorithm(null);
	}

	@Test
	public void testRobustnessForCallingWithoutModelSet() throws AlgorithmException {
		// Call algorithm without model set.
		this.model.getLearningAlgorithm(this.dataset).call();
		assertTrue(true); // this part must be reached
	}
}
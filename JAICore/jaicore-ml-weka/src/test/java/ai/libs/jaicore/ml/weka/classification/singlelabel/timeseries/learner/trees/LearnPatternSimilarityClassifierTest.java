package ai.libs.jaicore.ml.weka.classification.singlelabel.timeseries.learner.trees;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link LearnPatternSimilarityClassifier}.
 *
 * @author Julian Lienen
 *
 */
public class LearnPatternSimilarityClassifierTest {

	/**
	 * Classifier instance used within the tests.
	 */
	private LearnPatternSimilarityClassifier classifier;

	/**
	 * Number of segments used within the tests.
	 */
	private static final int NUM_SEGMENTS = 1;

	/**
	 * Number of trees used within the tests.
	 */
	private static final int NUM_TREES = 2;

	/**
	 * Maximum tree depth used within the tests.
	 */
	private static final int MAX_TREE_DEPTH = 10;

	/**
	 * Initialization called before all tests.
	 */
	@BeforeEach
	public void setup() {
		this.classifier = new LearnPatternSimilarityClassifier(42, NUM_TREES, MAX_TREE_DEPTH, NUM_SEGMENTS);
	}

	/**
	 * See
	 * {@link LearnPatternSimilarityClassifier#findNearestInstanceIndex(int[][])}.
	 */
	@Test
	public void findNearestInstanceIndexTest() {
		// Prepare test data with 2 instances, NUM_TREES trees with NUM_SEGMENTS segments
		final int[][][] trainLeafNodes = new int[][][] { { { 5 }, { 3 } }, { { 4 }, { 5 } } };
		this.classifier.setTrainLeafNodes(trainLeafNodes);

		final int[][] testLeafNodes = new int[][] { { 4 }, { 4 } };

		assertEquals("The nearest neighbor instance test selected the wrong instance.", 1, this.classifier.findNearestInstanceIndex(testLeafNodes));
	}
}
